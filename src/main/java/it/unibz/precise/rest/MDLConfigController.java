package it.unibz.precise.rest;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
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

import it.unibz.precise.model.Model;
import it.unibz.precise.model.validation.ValidationAdapter;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rep.PhaseRepository;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

/**
 * Exposes the configuration part in MDL files.
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
	path=MDLConfigController.RESOURCE_NAME,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLConfigController {
	
	public static final String RESOURCE_NAME = MDLFileController.RESOURCE_NAME + "/config";
	
	public static final String PATH_TO_FILE = MDLFileController.PATH_TO_FILE;
	
	public static final String FILE_SUFFIX = " (config)";
	
	@Autowired
	private ModelRepository repository;

	@Autowired
	private PhaseRepository phaseRepository;

	@Autowired
	private ValidationAdapter validator;
	
	/** Looks up the model of the given name in the repository and returns it as a {@link MDLConfigAST}. */
	private MDLConfigAST configByName(MDLContext context, String name) {
		Model model = repository.findByName(name);
		return context.configs().toMDL(model);
	}
	
	/** Exports the model of the given name as an {@link MDLFileAST} that only contains the configuration part. */
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.GET
	)
	public ResponseEntity<?> get(@PathVariable String name) {
		MDLContext context = MDLContext.create();
		MDLConfigAST config = configByName(context, name);
		if (config == null)
			return ResponseEntity.notFound().build();
		
		MDLFileAST mdlFile = context.files().createMDL();
		mdlFile.setConfiguration(config);
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, MDLFileController.getContentDisposition(name + FILE_SUFFIX))
			.body(mdlFile);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.PUT
	)
	@Transactional
	public ResponseEntity<?> set(
		HttpServletRequest request,
		@PathVariable String name,
		@RequestBody(required=false) MDLFileAST mdlFile,
		@RequestParam(name="use", required=false) String srcName
	) {
		Model model = repository.findByName(name);
		boolean toBeCreated = model == null;
		if (toBeCreated) {
			model = new Model();
			model.setName(name);
		}
		else if (!model.getState().getConfigInfo().isEditable())
			throw new IllegalStateException("Cannot configure an already configured model");
		
		MDLContext context = MDLContext.create();
		context.configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		repository.flush();
		
		MDLConfigAST config = mdlFile != null ? mdlFile.getConfiguration()
			: srcName != null ? configByName(context, srcName)
			: null;
		
		context.configs().updateEntity(config, model);
		
		Errors errors = validator.validate(model);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else {
			// We need to save the newly created phases manually.
			phaseRepository.save(model.getPhases());
			// If the model already existed, changes will be saved automatically.
			if (toBeCreated)
				repository.save(model);
		}
		return toBeCreated
			? ResponseEntity.created(URI.create(request.getRequestURL().toString())).build()
			: ResponseEntity.ok().build();
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<Model> clear(@PathVariable String name) {
		Model model = repository.findByName(name);
		MDLContext.create().configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		return new ResponseEntity<>(model, HttpStatus.OK);
	}

}
