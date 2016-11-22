package it.unibz.precise.rest;

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

import it.unibz.precise.model.Model;
import it.unibz.precise.model.validation.ValidationAdapter;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

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
	private ValidationAdapter validator;
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String FILE_EXT = ".mdl";
	public static final String PATH_TO_FILE = "/{name}" + FILE_EXT;
	

	@RequestMapping("/**")
	public ResponseEntity<?> fallback() {
		return ResponseEntity.notFound().build();
	}
	
	static String getContentDisposition(String name) {
		return FileDownload.getContentDisposition(name + MDLFileController.FILE_EXT);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE,
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
	
	@Transactional
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.PUT
	)
	public ResponseEntity<?> save(
		@PathVariable("name") String name,
		@RequestBody(required=false) MDLFileAST modelDTO,
		@RequestParam(defaultValue="false") boolean update,
		@RequestParam(name="use", required=false) String srcName)
	{
		if (modelDTO == null && srcName != null) {
			modelDTO = MDLContext.create().files().toMDL(repository.findByName(srcName));
			if (modelDTO == null)
				return ResponseEntity.notFound().build();
		}
		else if (update) {
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
		Model newModel = MDLContext.create().files().toEntity(modelDTO);
		newModel.setName(name);
		Errors errors = validator.validate(newModel);
		if (errors.hasErrors())
			throw new RepositoryConstraintViolationException(errors);
		else
			repository.save(newModel);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
}
