package it.unibz.precise.rest;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.Activity;
import it.unibz.precise.model.validation.ValidationAdapter;
import it.unibz.precise.rep.DependencyRepository;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rep.TaskRepository;
import it.unibz.precise.rep.ActivityRepository;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;
import it.unibz.precise.rest.mdl.ast.MDLCraftAST;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.precise.rest.mdl.ast.MDLActivityAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

/**
 * Exposes the diagram part in MDL files.
 * 
 * The structure of the files is the same as with complete MDL files,
 * but only the configuration part is considered.
 * This allows to use existing MDL files with this class.
 * 
 * @author MatthiasP
 *
 */
@RestController
@RequestMapping(
	path=MDLDiagramController.CTRL_PATH,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLDiagramController {
	
	public static final String CTRL_PATH = MDLFileController.CTRL_PATH + "/diagram";
	public static final String FILE_PATH = MDLFileController.FILE_PATH;
	public static final String FILE_EXT  = MDLFileController.FILE_EXT;
	
	public static final String FILE_SUFFIX = " (diagram)";
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private DependencyRepository dependencyRepository;

	@Autowired
	private ValidationAdapter validator;
	
	/** Looks up the model of the given name in the repository and returns it as a {@link MDLDiagramAST}. */
	private MDLDiagramAST diagramByName(MDLContext context, String name) {
		Model model = repository.findByName(name);
		return context.diagrams().toMDL(model);
	}
	
	/** Exports the model of the given name as an {@link MDLFileAST} that only contains the configuration part. */
	@RequestMapping(
		path=FILE_PATH + FILE_EXT,
		method=RequestMethod.GET
	)
	public ResponseEntity<?> get(@PathVariable String name) {
		MDLContext context = MDLContext.create();
		MDLDiagramAST dia = diagramByName(context, name);
		if (dia == null)
			return ResponseEntity.notFound().build();
		
		MDLFileAST mdlFile = context.files().createMDL();
		mdlFile.setDiagram(dia);
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, MDLFileController.getContentDisposition(name + FILE_SUFFIX))
			.body(mdlFile);
	}
	
	/**
	 * Imports the diagram of the given {@link MDLFileAST} into the model of the specified name.
	 * If no model such model exists, a new one is created.
	 * Instead of sending a file, a query parameter {@literal "use"} can be used to specify the name of
	 * an existing model whose diagram should be used. 
	 * @throws IllegalStateException if the configuration of the model is not editable (because a diagram exists)
	 */
	@RequestMapping(
		path=FILE_PATH,
		method=RequestMethod.PUT
	)
	@Transactional
	public ResponseEntity<?> set(
		HttpServletRequest request,
		@PathVariable String name,
		@RequestBody(required=false) MDLFileAST mdlFileSrc,
		@RequestParam(name="use", required=false) String srcName
	) {
		Model dstModel = repository.findByName(name);
		boolean toBeCreated = dstModel == null;
		if (toBeCreated) {
			dstModel = new Model();
			dstModel.setName(name);
		}
		
		// Setup the context
		MDLContext context = destinationContext(dstModel);
		
		// Clear the old diagram
		context.diagrams().updateEntity(MDLDiagramAST.EMPTY_DIAGRAM, dstModel);
		repository.flush();
		
		// Obtain diagram, either from file sent or existing model indicated by srcName
		MDLDiagramAST diaSrc = mdlFileSrc == null ? null : mdlFileSrc.getDiagram();
		if (diaSrc == null && srcName != null)
			diaSrc = diagramByName(MDLContext.create(), srcName);
		
		// Update the diagram part
		context.diagrams().updateEntity(diaSrc, dstModel);

		// The diagram part now possibly references activities not contained in the configuration part
		// -> fix this now
		// Before adding the new activities to the configuration, make sure their acronyms (shortNames)
		// do not clash with existing ones.
		
		Set<Activity> oldActivities = new HashSet<>(dstModel.getActivities());
		Set<Activity> newActivities = dstModel.getTasks().stream()
			.map(Task::getActivity)
			.filter(a -> !oldActivities.contains(a))
			.collect(Collectors.toSet());
		
		setNewActivityAcronyms(newActivities, oldActivities);
		// Now acronyms are unique -> add activity to the configuration
		newActivities.forEach(dstModel::addActivity);
		
		Errors errors = validator.validate(dstModel);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else {
			// We need to save the newly entities manually.
			activityRepository.save(dstModel.getActivities());
			taskRepository.save(dstModel.getTasks());
			dependencyRepository.save(dstModel.getDependencies());
			// If the model already existed, changes will be saved automatically.
			if (toBeCreated)
				repository.save(dstModel);
		}
		return toBeCreated
			? ResponseEntity.created(URI.create(request.getRequestURL().toString())).build()
			: ResponseEntity.noContent().build();
	}

	/**
	 * Check if acronyms of {@code newActivities} clash with those of {@code oldActivities} and attempt
	 * to assign new ones (adding numbers) if needed.
	 * @throws IllegalStateException if too many attempts in assigning a new number failed.
	 */
	private void setNewActivityAcronyms(Set<Activity> newActivities, Set<Activity> oldActivities) {
		Set<String> existingAcronyms = oldActivities.stream()
			.map(Activity::getShortName)
			.collect(Collectors.toSet());
		for (Activity a : newActivities) {
			String acr = a.getShortName();
			String originalAcr = acr;
			int max = 10;
			for (int i = 1; existingAcronyms.contains(acr); i++) {
				if (i >= max) {
					throw new IllegalStateException(
						"Cannot find unique acronym for activity " + a + ". Tried "
						+ originalAcr + '-' + 1 + " until " + originalAcr + '-' + max + '.'
					);
				}
				acr = originalAcr + '-' + i;
			}
			a.setShortName(acr);
			existingAcronyms.add(acr);		// cannot reuse new acronyms for multiple activities
		}
	}
	
	/**
	 * Create a {@link MDLContext} for updating the diagram of {@code dstModel} such that:
	 * <ul>
	 * <li> attributes, phases, crafts, and activities are cached and matched by name
	 * <li> only existing attributes, phases, and crafts can be used (other references set to null)
	 * <li> new activities may be created
	 * <li> conversion results are cached in both directions (MDL <--> entities)
	 * </ul>
	 */
	private MDLContext destinationContext(Model dstModel) {
		MDLContext context = MDLContext.create().switchStrictMode(false)
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
			.activities()
				.usingKeys(Activity::getName, MDLActivityAST::getName)
				.cacheInverseDirection(true)
				.context();
		
		// Perform one translation from entities to MDL to populate the cache with existing instances, ...
		context.files().toMDL(dstModel);
		// ... then prevent creation of further attributes, phases, and crafts.
		context
			.attributes().seal(true).context()
			.phases().seal(true).context()
			.crafts().seal(true).context();
			
		return context;
	}
	
	/** Clear the diagram of the model of the given name. */
	@RequestMapping(
		path=FILE_PATH,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<?> clear(@PathVariable String name) {
		Model model = repository.findByName(name);
		if (model == null)
			return ResponseEntity.notFound().build();
		
		MDLContext.create().diagrams().updateEntity(MDLDiagramAST.EMPTY_DIAGRAM, model);
		return ResponseEntity.noContent().build();
	}

}
