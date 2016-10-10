package it.unibz.precise.rest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.rep.ModelRepository;

@RestController
@RequestMapping(
	path=CSVFileController.RESOURCE_NAME,
	produces="text/csv"
)
public class CSVFileController {
	
	@Autowired
	private ModelRepository repository;

	public static final String RESOURCE_NAME = "/files";
	
	public static final String FILE_EXT = ".csv";
	
	public static final String PATH_TO_FILE = "/{name}" + FILE_EXT;
	
	@RequestMapping(
		path=PATH_TO_FILE,
		method=RequestMethod.GET,
		produces="text/csv"
	)
	public ResponseEntity<?> findOne(
		@PathVariable("name") String name,
		@RequestParam(name="separator", defaultValue=",") String sep,
		@RequestParam(name="listSeparator", defaultValue=", ") String listSep,
		@RequestParam(name="newline", defaultValue="\n") String nl
	) {
		Model model = repository.findByName(name);
		if (model == null)
			return ResponseEntity.notFound().build();
		
		String headerRow = headerRow(sep);
		
		String dataRows = model.getTasks().stream()
			.map(t -> dataRow(t, sep, listSep))
			.collect(Collectors.joining(nl));
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, FileDownload.getContentDisposition(name) + FILE_EXT)
			.body(String.join(nl, headerRow, dataRows));
	}
	
	private static String headerRow(String sep) {
		return String.join(sep, "Task", "Workers", "Duration", "Predecessors");
	}
	
	private static String dataRow(Task task, String sep, String listSep) {
		return Stream.of(
			task.getId(),
			task.getNumberOfWorkersNeeded(),
			task.getDurationDays(),
			task.getIn().stream()
				.map(Dependency::getSource)
				.map(Task::getId)
				.map(String::valueOf)
				.collect(Collectors.joining(listSep, "\"", "\""))
		).map(String::valueOf).collect(Collectors.joining(sep));
	}
	
}
