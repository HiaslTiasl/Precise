package it.unibz.precise.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.hateoas.ExposesResourceFor;
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
@ExposesResourceFor(MDLFileAST.class)
@RequestMapping(
	path=MDLFileController.RESOURCE_NAME,
	produces={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
)
public class MDLFileController {
	
	@Autowired
	private ModelRepository repository;

	@Autowired
	private Validator validator;
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String PATH_TO_FILE = "/{fileName}";
	
	public static final String MDL_FILE_EXT = ".mdl";
	
	private String getModelNameFor(String fileName) {
		return fileName.endsWith(MDL_FILE_EXT)
			? fileName.substring(0, fileName.length() - MDL_FILE_EXT.length())
			: null;
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.GET,
		produces=MediaType.APPLICATION_JSON_VALUE
	)
	public MDLFileAST findOne(@PathVariable("fileName") String fileName) {
		return MDLFileAST.translate(repository.findByName(getModelNameFor(fileName)));
	}
	
	@Transactional
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.PUT
	)
	public void save(
		@PathVariable("fileName") String fileName,
		@Valid @RequestBody MDLFileAST modelDTO,
		Errors errors,
		@RequestParam(defaultValue="false") boolean update)
	{
		String modelName = getModelNameFor(fileName);
		
		if (update) {
			// If explicitly requested, allow to overwrite a model with the given name
			Model oldModel = repository.findByName(modelName);
			if (oldModel != null) {
				// Delete old model before inserting the new one to avoid name conflict.
				// Flush to ensure proper order: first delete, then insert.
				// The transaction around the whole method ensures the deletion is rolled back
				// if the insertion fails.
				repository.delete(oldModel);
				repository.flush();		 
			}
		}
		Model newModel = modelDTO.toModel(modelName);
		validator.validate(newModel, errors);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else
			repository.save(newModel);
	}
	
}
