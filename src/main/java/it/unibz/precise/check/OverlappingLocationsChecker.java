package it.unibz.precise.check;

import it.unibz.precise.check.ConsistencyWarning.Category;
import it.unibz.precise.check.ConsistencyWarning.TaskLocation;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class OverlappingLocationsChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "overlap";
	
	public static final String WARNING_MESSAGE_OVERLAP   = "Location {0} overlaps with: {2}.";
	public static final String WARNING_MESSAGE_DUPLICATE = "Duplicate locations: {0}.";
	public static final String WARNING_MESSAGE_GLOBAL    = "The global locations {0} overlap all other locations.";
	
	@Override
	public Category getCategory() {
		return Category.COMPLETENESS;
	}
	
	@Override
	public String getType() {
		return WARNING_TYPE;
	}
	
	@Override
	public Stream<ConsistencyWarning> check(Model model) {
		return model.getTaskTypes().stream()
			.flatMap(this::checkType);
	}
	
	public Stream<ConsistencyWarning> checkType(TaskType taskType) {
		Collection<TaskLocation> taskLocations = taskType.getTasks().stream()
			.flatMap(this::taskLocations)
			.collect(Collectors.toList());
		
		if (taskLocations.size() <= 1)
			return Stream.empty();
		
		List<TaskLocation> globalLocations = taskLocations.stream()
			.filter(tl -> tl.getLocation().getLevel() == 0)
			.collect(Collectors.toList());
		
		if (!globalLocations.isEmpty())
			return Stream.of(warnGlobalLocations(globalLocations));
		
		Map<AttributeHierarchyNode, List<TaskLocation>> overlappings = taskLocations.stream()
			.collect(Collectors.groupingBy(tl -> tl.getLocation().getNode()));
		
		List<List<TaskLocation>> duplicates = overlappings.values().stream()
			.filter(l -> l.size() > 1)
			.collect(Collectors.toList());
		
		if (!duplicates.isEmpty())
			return duplicates.stream().map(this::warnDuplicateLocations);
		
		taskLocations.stream().forEachOrdered(tl -> {
			tl.getLocation().getNode().ancestorStream()
				.skip(1)
				.filter(overlappings::containsKey)
				.findFirst()
				.map(overlappings::get)
				.ifPresent(l -> l.add(tl));
		});
		
		return overlappings.values().stream()
			.filter(l -> l.size() > 1)
			.map(this::warnOverlappingLocations);
	}
	
	private Stream<TaskLocation> taskLocations(Task task) {
		List<Location> locations = task.getLocations();
		return IntStream.range(0, locations.size())
			.mapToObj(i -> new TaskLocation(task, locations.get(i), i));
	}
	
	private ConsistencyWarning warnGlobalLocations(List<TaskLocation> globalLocations) {
		String msg = MessageFormat.format(WARNING_MESSAGE_GLOBAL, locationsInTasks(globalLocations));
		return warning(msg, null, globalLocations);
	}
	
	private ConsistencyWarning warnDuplicateLocations(List<TaskLocation> duplicates) {
		String msg = MessageFormat.format(WARNING_MESSAGE_DUPLICATE, locationsInTasks(duplicates));
		return warning(msg, null, duplicates);
	}
	
	private ConsistencyWarning warnOverlappingLocations(List<TaskLocation> overlappingLocations) {
		String msg = MessageFormat.format(WARNING_MESSAGE_OVERLAP,
			overlappingLocations.get(0),
			locationsInTasks(overlappingLocations.subList(1, overlappingLocations.size()))
		);
		return warning(msg, null, overlappingLocations);
	}
	
	private Map<Task, List<TaskLocation>> groupByTasks(List<TaskLocation> taskLocationStream) {
		return taskLocationStream.stream()
			.sorted(Comparator.comparing(TaskLocation::getIndex))
			.collect(Collectors.groupingBy(TaskLocation::getTask));
	}
	
	private String locationsInTasks(List<TaskLocation> taskLocations) {
		return groupByTasks(taskLocations).entrySet().stream()
			.map(e -> locationsInTask(
				e.getValue().stream().map(TaskLocation::getLocation),
				e.getKey()
			))
			.collect(Collectors.joining(", "));
	}
	
	private String locationsInTask(Stream<Location> locations, Task task) {
		return locations.map(Location::getNode)
				.map(AttributeHierarchyNode::getPattern)
				.map(PatternEntry::toValueString)
				.collect(Collectors.joining(", "))
			+ " in task " + task.getShortIdentification();
	}

}
