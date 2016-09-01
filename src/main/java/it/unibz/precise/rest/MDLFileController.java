package it.unibz.precise.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
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
import it.unibz.precise.rest.mdl.ast.MDLFileAST;

@RestController
@RequestMapping(MDLFileController.RESOURCE_NAME)
@ExposesResourceFor(MDLFileAST.class)
public class MDLFileController {
	
	@Autowired
	private ModelRepository repository;

	@Autowired
	private Validator validator;
	
	public static final String RESOURCE_NAME = "files";
	
	@RequestMapping(
		path="/{name}.mdl",
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public MDLFileAST findOne(@PathVariable("name") String name) {
		return MDLFileAST.translate(repository.findByName(name));
	}
	
	@Transactional
	@RequestMapping(
		path="/{name}.mdl",
		method=RequestMethod.PUT
	)
	public void save(
		@PathVariable("name") String name,
		@Valid @RequestBody MDLFileAST modelDTO,
		Errors errors,
		@RequestParam(defaultValue="false") boolean update)
	{
		if (update) {
			// If explicitly requested, allow to overwrite a model with the given name
			Model oldModel = repository.findByName(name);
			if (oldModel != null) {
				// Delete old model before inserting the new one to avoid name conflict.
				// Flush to ensure proper order: first delete, then insert.
				// The transaction around the whole method ensures the deletion is rolled back
				// if the insertion fails.
				repository.delete(oldModel);
				repository.flush();		 
			}
		}
		Model newModel = modelDTO.toModel(name);
		validator.validate(newModel, errors);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else
			repository.save(newModel);
	}
	
}
