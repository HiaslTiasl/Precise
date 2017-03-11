package it.unibz.precise.rest;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.validation.ValidationAdapter;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rest.mdl.ast.MDLActivityAST;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes models as MDL files.
 * 
 * @author MatthiasP
 *
 */
@RestController
@RequestMapping(
	path=MDLFileController.CTRL_PATH,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLFileController {
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ValidationAdapter validator;
	
	public static final String CTRL_PATH = FileControllers.ROOT_PATH;
	public static final String FILE_PATH = FileControllers.NAME_PATTERN;		// Extension is optional and arbitrary (Spring exposes the same method with ".*" appended to the path).
	public static final String FILE_EXT = ".mdl";				// Used for exporting only; imports work with any extension, only the syntax counts.
	
	
	/** Returns the "Content-Disposition" HTTP Header value with a filename corresponding to the given model name. */
	static String getContentDisposition(String name) {
		return FileControllers.getContentDisposition(name, MDLFileController.FILE_EXT);
	}
	
	/** Returns the model of the specified name as a {@link MDLFileAST}. */
	@RequestMapping(
		path=FILE_PATH + FILE_EXT,
		method=RequestMethod.GET,
		produces=MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<?> findOne(@PathVariable("name") String name) {
		MDLFileAST mdl = MDLContext.create().files().toMDL(repository.findByName(name));
		return mdl == null
			? ResponseEntity.notFound().build()
			: ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(name))
				.body(mdl);
	}
	
	/**
	 * Imports the given {@link MDLFileAST} into as a new model of the specified name.
	 * Overwriting an existing model is not allowed unless the {@literal "update"} query parameters
	 * is set to explicitly indicate that overwriting is intended.
	 * Instead of sending a file, a query parameter {@literal "use"} can be used to specify the name of
	 * an existing model that should be used. 
	 */
	@Transactional
	@RequestMapping(
		path=FILE_PATH,
		method=RequestMethod.PUT
	)
	public ResponseEntity<?> save(
		HttpServletRequest request,
		@PathVariable("name") String name,
		@RequestBody(required=false) MDLFileAST modelDTO,
		@RequestParam(defaultValue="false") boolean update,
		@RequestParam(name="use", required=false) String srcName)
	{
		boolean toBeCreated = true;
		if (modelDTO == null && srcName != null) {
			modelDTO = MDLContext.create().files().toMDL(repository.findByName(srcName));
			if (modelDTO == null)
				return ResponseEntity.notFound().build();
		}
		else if (update) {
			// If explicitly requested, allow to overwrite a model with the given name
			Model oldModel = repository.findByName(name);
			if (oldModel != null) {
				toBeCreated = false;
				// Delete old model before inserting the new one to avoid name conflict.
				// Flush to ensure proper order: first delete, then insert.
				// The transaction around the whole method ensures the deletion is rolled back
				// if the insertion fails.
				repository.delete(oldModel);
				repository.flush();		 
			}
		}
		Model newModel = MDLContext.create().files().toEntity(modelDTO);
		newModel.setName(name);
		Errors errors = validator.validate(newModel);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else if (toBeCreated)
			repository.save(newModel);
		return toBeCreated
			? ResponseEntity.created(URI.create(request.getRequestURL().toString())).build()
			: ResponseEntity.noContent().build();
	}
	
	@RequestMapping(
		path="/replicated" + FILE_PATH + FILE_EXT,
		method=RequestMethod.GET,
		produces=MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<?> replicate(
		@PathVariable("name") String name,
		@RequestParam("repeat") int repeat,
		@RequestParam("depToTaskRatio") double depToTaskRatio
	) {
		Model originalModel = repository.findByName(name);
		if (originalModel == null)
			return ResponseEntity.notFound().build();
		
		Model replicatedModel = copy(originalModel);
		List<List<Task>> tasksByModel = new ArrayList<>(repeat);
		
		// Direct copy is first replication
		tasksByModel.add(new ArrayList<>(replicatedModel.getTasks())); 
		// Generate remaining copies
		for (int i = 1; i < repeat; i++) {
			MDLContext context = replicationContext(replicatedModel);
			MDLFileAST mdlFile = context.files().toMDL(originalModel);
			MDLConfigAST mdlConfig = mdlFile.getConfiguration();
			MDLDiagramAST mdlDiagram = mdlFile.getDiagram();
			for (MDLActivityAST a : mdlConfig.getActivities()) {
				a.setName(a.getName() + '-' + i);
				a.setShortName(a.getShortName() + '-' + i);
				replicatedModel.addActivity(context.activities().toEntity(a));
			}
			List<MDLTaskAST> mdlTasks = mdlDiagram.getTasks();
			List<Task> tasks = new ArrayList<>(mdlTasks.size());
			for (MDLTaskAST t : mdlTasks) {
				Task task = context.tasks().toEntity(t);
				tasks.add(task);
				replicatedModel.addTask(task);
			}
			tasksByModel.add(tasks);
			for (MDLDependencyAST d : mdlDiagram.getDependencies())
				replicatedModel.addDependency(context.dependencies().toEntity(d));
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();
		double connectedProbability = 2 * depToTaskRatio / (repeat - 1);
		for (int i = 0; i < repeat - 1; i++) {
			for (int j = i + 1; j < repeat; j++) {
				double p = random.nextDouble();
				if (p >= connectedProbability) {
					List<Task> sourceModelTasks = tasksByModel.get(i);
					List<Task> targetModelTasks = tasksByModel.get(j);
					Dependency d = new Dependency();
					d.setSource(sourceModelTasks.get(random.nextInt(sourceModelTasks.size())));		// N.B. size is actually always the same
					d.setTarget(targetModelTasks.get(random.nextInt(targetModelTasks.size())));
					replicatedModel.addDependency(d);
				}
			}
		}
		
		MDLFileAST mdlResult = MDLContext.create().files().toMDL(replicatedModel);
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(name + " x" + repeat))
			.body(mdlResult);
	}
	
	static MDLContext replicationContext(Model model) {
		MDLContext context = MDLDiagramController.destinationContext(model);
		// We want to create new activities
		context.activities().cacheInverseDirection(false);
		return context;
	}
	
	private Model copy(Model model) {
		MDLContext context = MDLContext.create();
		return context.files().toEntity(context.files().toMDL(model));
	}
	
}
