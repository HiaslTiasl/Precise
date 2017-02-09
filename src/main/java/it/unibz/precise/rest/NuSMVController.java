package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Model;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.precise.smv.ModelToNuSMVTranslator;

/**
 * Exports a model as a NuSMV module file.
 * 
 * @author MatthiasP
 *
 */
@RestController
@RequestMapping(
	path=NuSMVController.RESOURCE_NAME,
	produces="text/smv"
)
public class NuSMVController {

	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ModelToNuSMVTranslator translator;
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String FILE_EXT = ".smv";				// Used for exporting only; imports work with any extension, only the syntax counts.
	public static final String PATH_TO_FILE = "/{name}";		// Extension is optional and arbitrary for imports (Spring exposes the same method with ".*" appended to the path).
	
	/** Returns the "Content-Disposition" HTTP Header value with a filename corresponding to the given model name. */
	static String getContentDisposition(String name) {
		return FileDownload.getContentDisposition(name + FILE_EXT);
	}
	
	/** Exports the NuSMV file. */
	@RequestMapping(
		path=PATH_TO_FILE + FILE_EXT,
		method=RequestMethod.GET,
		produces="text/smv"
	)
	public ResponseEntity<?> getAsSMV(@PathVariable("name") String name) {
		Model model = repository.findByName(name);
		return model == null
			? ResponseEntity.notFound().build()
			: ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(name))
				.body(translator.translate(model));
	}

}
