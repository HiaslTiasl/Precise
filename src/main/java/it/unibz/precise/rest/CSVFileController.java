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
import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.Activity;
import it.unibz.precise.rep.ModelRepository;

/**
 * Exports a model as a CSV file.
 * 
 * The file has the following columns:
 * 
 * <table border="1">
 * 	<thead>
 * 		<tr>
 * 			<th>Name</th>
 * 			<th>Acronym</th>
 * 			<th>ID</th>
 * 			<th>Crew Size</th>
 * 			<th>Crew Count</th>
 * 			<th>Duration</th>
 * 			<th>Unit of Measure</th>
 * 			<th>Total Quantity</th>
 * 			<th>Quantity Per Day</th>
 * 			<th>Man-hours</th>
 * 			<th>Locations</th>
 * 			<th>Predecessors</th>
 * 		</tr>
 * 	</thead>
 * 	<tbody>
 * 		<tr>
 * 			<th>Concrete Pouring</th>
 * 			<th>C</th>
 * 			<th>39</th>
 * 			<th>3</th>
 * 			<th>1</th>
 * 			<th>40</th>
 * 			<th>sqm</th>
 * 			<th>1000</th>
 * 			<th>25</th>
 * 			<th>960</th>
 * 			<th>*-*</th>
 * 			<th>B#38, G#43</th> 
 * 		</tr>
 * 	</tbody>
 * </table>
 * 
 * @author MatthiasP
 *
 */
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
	
	/**
	 * Exports the CSV file.
	 * The format can be configured by setting custom separators in URL query parameters.
	 */
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
		
		// List tasks in topological order.
		// Tasks in a SCCs are sorted lexicographically by short identification.
		DiagramGraph graph = DiagramGraph.of(model);
		List<Task> orderedTasks = sccTarjan.findSCCs(graph).stream()
			.flatMap(l -> l.stream().sorted(Task.shortIdentificationComparator()))
			.collect(Collectors.toList());
		
		String dataRows = orderedTasks.stream()
			.map(t -> dataRow(t, sep, locSep, taskSep))
			.collect(Collectors.joining(nl));
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, FileControllers.getContentDisposition(name, FILE_EXT))
			.body(String.join(nl, headerRow, dataRows));
	}
	
	/** Returns the header row of the CSV. */
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
	
	/** Returns a data row corresponding to the given task. */
	private String dataRow(Task task, String sep, String locSep, String taskSep) {
		Activity activity = task.getActivity();
		Pitch pitch = task.getPitch();
		return Stream.of(
			activity.getName(),
			activity.getShortName(),
			task.getId(),
			pitch.getCrewSize(),
			pitch.getCrewCount(),
			pitch.getDurationDays(),
			activity.getUnitOfMeasure(),
			pitch.getTotalQuantity(),
			pitch.getQuantityPerDay(),
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
		.map(cell -> ESCAPE + Objects.toString(cell, "") + ESCAPE)		// Some fields may contain separators (e.g. name, locations, predecessors)
		.collect(Collectors.joining(sep));
	}
	
}
