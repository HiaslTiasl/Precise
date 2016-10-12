package it.unibz.precise.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Model;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rep.PhaseRepository;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

@RestController
@ExposesResourceFor(MDLConfigAST.class)
@RequestMapping(
	path=MDLConfigController.RESOURCE_NAME,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLConfigController {
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String PATH_TO_FILE = MDLFileController.PATH_TO_FILE + "/config";
	
	public static final String FILE_SUFFIX = " (config)";
	
	@Autowired
	private ModelRepository repository;

	@Autowired
	private PhaseRepository phaseRepository;

	@Autowired
	private Validator validator;
	
	private MDLConfigAST configByName(MDLContext context, String name) {
		Model model = repository.findByName(name);
		return context.configs().toMDL(model);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.GET
	)
	public ResponseEntity<MDLConfigAST> get(@PathVariable String name) {
		MDLConfigAST config = configByName(new MDLContext(), name);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, MDLFileController.getContentDisposition(name + FILE_SUFFIX))
				.body(config);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.PUT
	)
	@Transactional
	public void set(
		@PathVariable String name,
		@Valid @RequestBody(required=false) MDLConfigAST config,
		Errors errors,
		@RequestParam(name="use", required=false) String srcName
	) {
		Model model = repository.findByName(name);
		
		if (!model.getState().getConfigInfo().isEditable())
			throw new IllegalStateException("Cannot configure an already configured model");
		
		MDLContext context = new MDLContext();
		context.configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		repository.flush();
		
		if (config == null && srcName != null)
			config = configByName(context, srcName);
		
		context.configs().updateEntity(config, model);
		
		validator.validate(model, errors);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else {
			// We need to save the newly created phases manually.
			// Changes to the model table will be saved automatically.
			phaseRepository.save(model.getPhases());
			//repository.save(model);
		}
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<Model> clear(@PathVariable String name) {
		Model model = repository.findByName(name);
		new MDLContext().configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		return new ResponseEntity<>(model, HttpStatus.OK);
	}

}
