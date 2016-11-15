package it.unibz.precise.check;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import it.unibz.precise.check.ConsistencyWarning.TaskLocation;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

@Service
public class OverlappingLocationsChecker implements ConsistencyChecker {

	public static final String WARNING_TYPE = "overlap";
	
	public static final String WARNING_MESSAGE_OVERLAP = "The following locations overlap each other: ";
	public static final String WARNING_MESSAGE_DUPLICATE = "The following locations are duplicates: ";
	public static final String WARNING_MESSAGE_GLOBAL = "The following locations are global and overlap all other locations: ";
	
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
		String msg = WARNING_MESSAGE_GLOBAL + indicesInTasks(globalLocations);
		return new ConsistencyWarning(WARNING_TYPE, msg, null, globalLocations);
	}
	
	private ConsistencyWarning warnDuplicateLocations(List<TaskLocation> duplicates) {
		String msg = WARNING_MESSAGE_DUPLICATE + indicesInTasks(duplicates);
		return new ConsistencyWarning(WARNING_TYPE, msg, null, duplicates);
	}
	
	private ConsistencyWarning warnOverlappingLocations(List<TaskLocation> overlappingLocations) {
		String msg = WARNING_MESSAGE_OVERLAP + indicesInTasks(overlappingLocations);
		return new ConsistencyWarning(WARNING_TYPE, msg, null, overlappingLocations);
	}
	
	private Map<Task, List<TaskLocation>> groupByTasks(List<TaskLocation> taskLocationStream) {
		return taskLocationStream.stream()
			.sorted(Comparator.comparing(TaskLocation::getIndex))
			.collect(Collectors.groupingBy(TaskLocation::getTask));
	}
	
	private String indicesInTasks(List<TaskLocation> globalLocations) {
		return groupByTasks(globalLocations).entrySet().stream()
			.map(e -> "locations "
				+ e.getValue().stream()
					.map(TaskLocation::getIndex)
					.map(String::valueOf)
					.collect(Collectors.joining(", "))
				+ " in task " + e.getKey().getShortIdentification()
			)
			.collect(Collectors.joining(", "));
	}

}
