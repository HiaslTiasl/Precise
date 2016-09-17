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
import it.unibz.precise.rep.PhaseRepository;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLFileContext;

@RestController
@ExposesResourceFor(MDLConfigAST.class)
public class MDLConfigController {
	
	private static final String BASE_PATH = "/files/{name}.mdl/config";
	
	@Autowired
	private ModelRepository modelRepository;
	
	@Autowired
	private PhaseRepository phaseRepository;
	
	private MDLConfigAST configByName(String name) {
		Model model = modelRepository.findByName(name);
		return new MDLConfigAST(new MDLFileContext(), model);
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.GET
	)
	@Transactional
	public ResponseEntity<MDLConfigAST> get(@PathVariable String name) {
		MDLConfigAST config = configByName(name);
		return new ResponseEntity<>(config, HttpStatus.OK);
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.PUT
	)
	@Transactional
	public void set(@PathVariable String name, @RequestBody MDLConfigAST config/*, @RequestParam("srcName") String srcName*/) {
		Model model = modelRepository.findByName(name);
		if (model.isBuildingConfigured())
			throw new IllegalStateException("Cannot configure an already configured model");
		config.applyTo(model);
		phaseRepository.save(model.getPhases());
	}
	
	@RequestMapping(
		path=BASE_PATH,
		method=RequestMethod.DELETE
	)
	@Transactional
	public ResponseEntity<Model> clear(@PathVariable String name) {
		Model model = modelRepository.findByName(name);
		model.setAttributes(null);
		model.setPhases(null);
		return new ResponseEntity<>(model, HttpStatus.OK);
	}

}
