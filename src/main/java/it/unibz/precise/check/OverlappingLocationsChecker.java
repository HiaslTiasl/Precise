package it.unibz.precise.check;

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

/**
 * Checks if the diagram has task definitions with overlapping locations.
 * Two locations overlap if they are equal or one is contained in the other.
 * A location {@code l1} is contained in another location {@code l2} if the
 * {@link AttributeHierarchyNode node} of {@code l1} is a descendant of the node
 * of {@code l2}.
 * 
 * @author MatthiasP
 *
 */
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
			.flatMap(this::checkType);		// Check all types individually
	}
	
	/**
	 * Check if {@code taskType} has overlapping locations by iterating
	 * through all locations of all task boxes referring to {@code taskType}
	 * and marking the corresponding nodes in the CA hierarchy.
	 */
	public Stream<ConsistencyWarning> checkType(TaskType taskType) {
		// Collect all locations for the task definitions, keeping a reference to the containing task box.
		Collection<TaskLocation> taskLocations = taskType.getTasks().stream()
			.flatMap(this::taskLocations)
			.collect(Collectors.toList());
		
		// There cannot be overlappings if there are less than two locations.
		if (taskLocations.size() < 2)
			return Stream.empty();
		
		// If there is a global location, it overlaps all others.
		List<TaskLocation> globalLocations = taskLocations.stream()
			.filter(tl -> tl.getLocation().getLevel() == 0)
			.collect(Collectors.toList());
		
		if (!globalLocations.isEmpty())
			return Stream.of(warnGlobalLocations(globalLocations));
		
		// Group locations by their CA nodes, i.e. map CAs to the list of corresponding locations.
		Map<AttributeHierarchyNode, List<TaskLocation>> overlappings = taskLocations.stream()
			.collect(Collectors.groupingBy(tl -> tl.getLocation().getNode()));
		
		// Multiple locations with the same CA represent duplicates, possibly across task boxes.
		List<List<TaskLocation>> duplicates = overlappings.values().stream()
			.filter(l -> l.size() > 1)
			.collect(Collectors.toList());
		
		if (!duplicates.isEmpty())
			return duplicates.stream().map(this::warnDuplicateLocations);
		
		// Find overlappings that are not duplicates:
		taskLocations.stream().forEachOrdered(tl -> {
			tl.getLocation().getNode().ancestorStream()		// Walk up the hierarchy of each node, ...
				.skip(1)									// ... starting with its parent.
				.filter(overlappings::containsKey)			// When an ancestor is already marked...
				.findFirst()								// (consider first only)
				.map(overlappings::get)
				.ifPresent(l -> l.add(tl));					// ... add this location to the overlappings of that ancestor.
		});
		
		// Now all potential overlappings are found
		return overlappings.values().stream()
			.filter(l -> l.size() > 1)
			.map(this::warnOverlappingLocations);
	}
	
	/** Return a stream of {@link TaskLocation}s for all locations of the given task. */
	private Stream<TaskLocation> taskLocations(Task task) {
		List<Location> locations = task.getLocations();
		return IntStream.range(0, locations.size())
			.mapToObj(i -> new TaskLocation(task, locations.get(i), i));
	}
	
	/** Produce a warning for the given gloval locations. */
	private ConsistencyWarning warnGlobalLocations(List<TaskLocation> globalLocations) {
		String msg = MessageFormat.format(WARNING_MESSAGE_GLOBAL, locationsInTasks(globalLocations));
		return warning(msg, null, globalLocations);
	}
	
	/** Produce a warning for the given duplicate locations. */
	private ConsistencyWarning warnDuplicateLocations(List<TaskLocation> duplicates) {
		String msg = MessageFormat.format(WARNING_MESSAGE_DUPLICATE, locationsInTasks(duplicates));
		return warning(msg, null, duplicates);
	}
	
	/** Produce a warning for the given overlapping locations. */
	private ConsistencyWarning warnOverlappingLocations(List<TaskLocation> overlappingLocations) {
		String msg = MessageFormat.format(WARNING_MESSAGE_OVERLAP,
			overlappingLocations.get(0),
			locationsInTasks(overlappingLocations.subList(1, overlappingLocations.size()))
		);
		return warning(msg, null, overlappingLocations);
	}
	
	/** Group the given {@link TaskLocation}s by their node in the CA hierarchy. */
	private Map<Task, List<TaskLocation>> groupByTasks(List<TaskLocation> taskLocationStream) {
		return taskLocationStream.stream()
			.sorted(Comparator.comparing(TaskLocation::getIndex))
			.collect(Collectors.groupingBy(TaskLocation::getTask));
	}
	
	/** Convert the given {@link TaskLocation}s to a string. */
	private String locationsInTasks(List<TaskLocation> taskLocations) {
		return groupByTasks(taskLocations).entrySet().stream()
			.map(e -> locationsInTask(
				e.getValue().stream().map(TaskLocation::getLocation),
				e.getKey()
			))
			.collect(Collectors.joining(", "));
	}
	
	/** Convert the given {@link TaskLocation}s in the given {@link Task} to a string. */
	private String locationsInTask(Stream<Location> locations, Task task) {
		return locations.map(Location::getNode)
				.map(n -> AttributeHierarchyNode.toPattern(n, task.getType().getPhase().getAttributeHierarchyLevels()))
				.map(PatternEntry::toValueString)
				.collect(Collectors.joining(", "))
			+ " in task " + task.getShortIdentification();
	}

}
