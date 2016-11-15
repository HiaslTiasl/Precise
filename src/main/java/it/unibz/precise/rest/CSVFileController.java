package it.unibz.precise.rest;

import java.util.List;
import java.util.Objects;
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

import it.unibz.precise.check.DiagramGraph;
import it.unibz.precise.graph.SCCTarjan;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rep.ModelRepository;

@RestController
@RequestMapping(
	path=CSVFileController.RESOURCE_NAME,
	produces="text/csv"
)
public class CSVFileController {
	
	private static final String ESCAPE = "\"";
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private SCCTarjan sccTarjan;
	
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
		@RequestParam(name="locSeparator", defaultValue=", ") String locSep,
		@RequestParam(name="taskSeparator", defaultValue=", ") String taskSep,
		@RequestParam(name="newline", defaultValue="\r\n") String nl
	) {
		Model model = repository.findByName(name);
		if (model == null)
			return ResponseEntity.notFound().build();
		
		String headerRow = headerRow(sep);
		
		DiagramGraph graph = new DiagramGraph(model);
		List<Task> orderedTasks = sccTarjan.findSCCs(graph).stream()
			.flatMap(l -> l.stream().sorted(Task.shortIdentificationComparator()))
			.collect(Collectors.toList());
		
		String dataRows = orderedTasks.stream()
			.map(t -> dataRow(t, sep, locSep, taskSep))
			.collect(Collectors.joining(nl));
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, FileDownload.getContentDisposition(name + FILE_EXT))
			.body(String.join(nl, headerRow, dataRows));
	}
	
	private static String headerRow(String sep) {
		return String.join(sep,
			"Name",
			"Acronym",
			"ID",
			"Crew Size",
			"Crew Count",
			"Duration",
			"Unit of Measure",
			"Total Quantity",
			"Quantity Per Day",
			"Man-hours",
			"Locations",
			"Predecessors"
		);
	}
	
	private String dataRow(Task task, String sep, String locSep, String taskSep) {
		TaskType type = task.getType();
		return Stream.of(
			type.getName(),
			type.getShortName(),
			task.getId(),
			task.getCrewSize(),
			task.getCrewCount(),
			task.getDurationDays(),
			type.getUnitOfMeasure(),
			task.getTotalQuantity(),
			task.getQuantityPerDay(),
			task.getManHours(),
			task.getLocationPatterns().stream()
				.map(PatternEntry::toValueString)
				.collect(Collectors.joining(locSep)),
			task.getIn().stream()
				.map(Dependency::getSource)
				.sorted(Task.shortIdentificationComparator())
				.map(Task::getShortIdentification)
				.map(String::valueOf)
				.collect(Collectors.joining(taskSep))
		)
		.map(cell -> ESCAPE + Objects.toString(cell, "") + ESCAPE)
		.collect(Collectors.joining(sep));
	}
	
}
