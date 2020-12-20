package it.unibz.precise.check;

import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.util.Util;

import static java.util.stream.Collectors.toSet;

/**
 * Utility methods regarding consistency checks.
 * 
 * @author MatthiasP
 *
 */
public class CheckerUtil {

	/** Do not instantiate this class. */
	private CheckerUtil() {
	}
	
	/** Returns a stream of all dependencies that span between two of the given tasks. */
	public static Stream<Dependency> restrictDependenciesByTasks(Set<Task> taskSet) {
		return taskSet.stream()
			.map(Task::getOut)
			.flatMap(List::stream)
			.filter(d -> taskSet.contains(d.getTarget()));
	}
	
	/**
	 * Restricts the diagram diagram part by the given tasks.
	 * Returns a stream of the given tasks and all dependencies between them.
	 */
	public static Stream<BaseEntity> restrictDiagramByTasks(Model model, BitSet mask) {
		Set<Task> taskSet = mask.stream().mapToObj(model.getTasks()::get).collect(toSet());
		return Stream.concat(
			taskSet.stream(),
			restrictDependenciesByTasks(taskSet)
		);
	}

}
