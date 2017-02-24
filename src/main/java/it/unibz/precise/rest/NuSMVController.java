package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	path=NuSMVController.CTRL_PATH,
	produces="text/smv"
)
public class NuSMVController {

	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ModelToNuSMVTranslator translator;
	
	public static final String CTRL_PATH = FileControllers.ROOT_PATH;
	public static final String FILE_PATH = FileControllers.NAME_PATTERN;	
	public static final String FILE_EXT = ".smv";				// Used for exporting only; imports work with any extension, only the syntax counts.
	
	/** Exports the NuSMV file. */
	@RequestMapping(
		path=FILE_PATH + FILE_EXT,
		method=RequestMethod.GET,
		produces="text/smv"
	)
	public ResponseEntity<?> getAsSMV(
		@PathVariable("name") String name,
		@RequestParam(name="ignoreSimple", defaultValue="false") boolean ignoreSimple
	) {
		Model model = repository.findByName(name);
		return model == null
			? ResponseEntity.notFound().build()
			: ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, FileControllers.getContentDisposition(name, FILE_EXT))
				.body(translator.translate(model, ignoreSimple));
	}

}
