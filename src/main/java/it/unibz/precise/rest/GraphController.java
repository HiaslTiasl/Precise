package it.unibz.precise.rest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.check.ModelToGraphTranslator;
import it.unibz.precise.check.TaskUnitNode;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Model;
import it.unibz.precise.rep.ModelRepository;

@RestController
@RequestMapping(
	path=GraphController.RESOURCE_NAME,
	produces="text/graph"
)
public class GraphController {

	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ModelToGraphTranslator translator;
	
	public static final String RESOURCE_NAME = "/files";
	
	public static final String FILE_EXT = ".graph";				// Used for exporting only; imports work with any extension, only the syntax counts.
	public static final String PATH_TO_FILE = "/{name}";		// Extension is optional and arbitrary (Spring exposes the same method with ".*" appended to the path).
	
	/** Returns the "Content-Disposition" HTTP Header value with a filename corresponding to the given model name. */
	static String getContentDisposition(String name) {
		return FileDownload.getContentDisposition(name + FILE_EXT);
	}
	
	@RequestMapping(
		path=PATH_TO_FILE + FILE_EXT,
		method=RequestMethod.GET,
		produces="text/graph"
	)
	public ResponseEntity<?> getAsGraph(@PathVariable("name") String name) {
		Model model = repository.findByName(name);
		return model == null
			? ResponseEntity.notFound().build()
			: ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(name))
				.body(serialize(translator.translate(model)));
	}
	
	/** Returns a textual representation of the given graph. */
	private String serialize(DisjunctiveGraph<TaskUnitNode> graph) {
		return Stream.concat(
			graph.arcs().stream(),
			graph.edges().stream()
		).map(Object::toString)
		.collect(Collectors.joining("\n"));
	}

}
