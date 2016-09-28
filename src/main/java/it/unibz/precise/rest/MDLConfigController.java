package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Model;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

@RestController
@ExposesResourceFor(MDLConfigAST.class)
public class MDLConfigController {
	
	private static final String BASE_PATH = "/files/{name}.mdl/config";
	
	@Autowired
	private ModelRepository modelRepository;
	
	private MDLConfigAST configByName(MDLContext context, String name) {
		Model model = modelRepository.findByName(name);
		return context.configs().toMDL(model);
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.GET
	)
	public ResponseEntity<MDLConfigAST> get(@PathVariable String name) {
		MDLConfigAST config = configByName(new MDLContext(), name);
		return new ResponseEntity<>(config, HttpStatus.OK);
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.PUT
	)
	@Transactional
	public void set(
		@PathVariable String name,
		@RequestBody(required=false) MDLConfigAST config,
		@RequestParam(name="use", required=false) String srcName
	) {
		Model model = modelRepository.findByName(name);
		
		if (!model.getState().getConfigInfo().isEditable())
			throw new IllegalStateException("Cannot configure an already configured model");
		
		MDLContext context = new MDLContext();
		context.configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		modelRepository.flush();
		
		if (config == null && srcName != null)
			config = configByName(context, srcName);
		
		context.configs().updateEntity(config, model);
		modelRepository.save(model);
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<Model> clear(@PathVariable String name) {
		Model model = modelRepository.findByName(name);
		new MDLContext().configs().updateEntity(MDLConfigAST.EMPTY_CONFIG, model);
		return new ResponseEntity<>(model, HttpStatus.OK);
	}

}
