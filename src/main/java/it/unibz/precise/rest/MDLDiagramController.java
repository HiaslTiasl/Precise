package it.unibz.precise.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.model.validation.ValidationAdapter;
import it.unibz.precise.rep.DependencyRepository;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rep.TaskRepository;
import it.unibz.precise.rep.TaskTypeRepository;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLCraftAST;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;
import it.unibz.precise.rest.mdl.conversion.MDLTranslator;

@RestController
@ExposesResourceFor(MDLConfigAST.class)
@RequestMapping(
	path=MDLDiagramController.RESOURCE_NAME,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLDiagramController {
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String PATH_TO_FILE = MDLFileController.PATH_TO_FILE + "/diagram";
	
	public static final String FILE_SUFFIX = " (diagram)";
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private TaskTypeRepository taskTypeRepository;

	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private DependencyRepository dependencyRepository;

	@Autowired
	private ValidationAdapter validator;
	
	private <MDL> MDL mdlByName(MDLTranslator<Model, MDL> translator, String name) {
		Model model = repository.findByName(name);
		return translator.toMDL(model);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.GET
	)
	public ResponseEntity<?> get(@PathVariable String name) {
		MDLDiagramAST dia = mdlByName(MDLContext.create().diagrams(), name);
		if (dia == null)
			return ResponseEntity.notFound().build();
		
		MDLFileAST mdlFile = new MDLFileAST();
		mdlFile.setDiagram(dia);
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, MDLFileController.getContentDisposition(name + FILE_SUFFIX))
			.body(mdlFile);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.PUT
	)
	@Transactional
	public void set(
		@PathVariable String name,
		@RequestBody(required=false) MDLFileAST mdlFileSrc,
		@RequestParam(name="use", required=false) String srcName
	) {
		Model dstModel = repository.findByName(name);
		
		MDLContext context = destinationContext();

		// Perform one translation from entities to MDL to populate the cache, ...
		context.files().toMDL(dstModel);
		// ... then prevent creation of further attributes, phases, and crafts.
		context.attributes().seal();
		context.phases().seal();
		context.crafts().seal();
		
		context.diagrams().updateEntity(MDLDiagramAST.EMPTY_DIAGRAM, dstModel);
		repository.flush();
		
		MDLDiagramAST diaSrc = mdlFileSrc == null ? null : mdlFileSrc.getDiagram();
		if (diaSrc == null && srcName != null)
			diaSrc = mdlByName(MDLContext.create().diagrams(), srcName);
		
		Set<TaskType> oldTaskTypes = new HashSet<>(dstModel.getTaskTypes());
		Set<String> acronyms = oldTaskTypes.stream()
			.map(TaskType::getShortName)
			.collect(Collectors.toSet());
		
		context.diagrams().updateEntity(diaSrc, dstModel);
		
		Set<TaskType> newTaskTypes = dstModel.getTasks().stream()
			.map(Task::getType)
			.filter(tt -> !oldTaskTypes.contains(tt))
			.collect(Collectors.toSet());
		
		for (TaskType tt : newTaskTypes) {
			String acr = tt.getShortName();
			String originalAcr = acr;
			int max = 10;
			for (int i = 1; acronyms.contains(acr); i++) {
				if (i >= max) {
					throw new IllegalStateException(
						"Cannot find unique acronym for task type " + tt + ". Tried "
						+ originalAcr + '-' + 1 + " until " + originalAcr + '-' + max + '.'
					);
				}
				acr = originalAcr + '-' + i;
			}
			tt.setShortName(acr);
			acronyms.add(acr);
			dstModel.addTaskType(tt);
		}
		
		Errors errors = validator.validate(dstModel);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else {
			// We need to save the newly created phases manually.
			// Changes to the model table will be saved automatically.
			taskTypeRepository.save(dstModel.getTaskTypes());
			taskRepository.save(dstModel.getTasks());
			dependencyRepository.save(dstModel.getDependencies());
			//repository.save(model);
		}
	}
	
	private MDLContext destinationContext() {
		return MDLContext.create().switchStrictMode(false)
			//.files().context()
			//.models().context()
			//.configs().context()
			//.diagrams().context()
			.attributes()
				.usingKeys(Attribute::getName, MDLAttributeAST::getName)
				.cacheInverseDirection(true)
				.context()
			.phases()
				.usingKeys(Phase::getName, MDLPhaseAST::getName)
				.cacheInverseDirection(true)
				.context()
			.crafts()
				.usingKeys(Craft::getName, MDLCraftAST::getName)
				.cacheInverseDirection(true)
				.context()
			.taskTypes()
				.usingKeys(TaskType::getName, MDLTaskTypeAST::getName)
				.cacheInverseDirection(true)
				.context();
			//.tasks().context()
			//.dependencies().context()
			//.scopes().context()
			//.orderSpecs().context()
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<Model> clear(@PathVariable String name) {
		Model model = repository.findByName(name);
		MDLContext.create().diagrams().updateEntity(MDLDiagramAST.EMPTY_DIAGRAM, model);
		return new ResponseEntity<>(model, HttpStatus.OK);
	}

}