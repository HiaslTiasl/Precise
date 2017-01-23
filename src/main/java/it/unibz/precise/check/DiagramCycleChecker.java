package it.unibz.precise.check;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

/**
 * Checks a process models for cycles at the diagram level.
 * 
 * @author MatthiasP
 * @see DiagramGraph
 *
 */
@Service
public class DiagramCycleChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "cycles";
	
	public static final String WARNING_MESSAGE = "The following tasks contain cycles: {0}";

	@Autowired
	private SCCFinder sccFinder;
	
	@Override
	public Category getCategory() {
		return Category.SATISFIABILITY;
	}
	
	@Override
	public String getType() {
		return WARNING_TYPE;
	}
	
	/**
	 * Find non-trivial SCCs in the diagram of {@code model} and return a {@link ConsistencyWarning}
	 * for each of them.
	 */
	@Override
	public Stream<ConsistencyWarning> check(Model model) {
		List<List<Task>> sccs = sccFinder.findSCCs(DiagramGraph.of(model));
		return sccs.stream()
			.filter(SCCFinder::isNonTrivialComponent)
			.map(this::warning);
	}
	
	/** Produce a warning about the given SCC. */
	private ConsistencyWarning warning(List<Task> scc) {
		// Put tasks into a set for fast lookup
		Set<Task> taskSet = new HashSet<>(scc);
		
		// Project dependencies to the tasks in scc
		Stream<Dependency> dependencies = scc.stream()
			.map(Task::getOut)
			.flatMap(List::stream)
			.filter(d -> taskSet.contains(d.getTarget()));
		
		// Collect tasks and dependencies in a single list
		List<BaseEntity> entities = Stream.concat(scc.stream(), dependencies).collect(Collectors.toList());
		
		// Create message
		String msg = MessageFormat.format(WARNING_MESSAGE, 
			scc.stream()
				.sorted(Task.shortIdentificationComparator())
				.map(Task::getShortIdentification)
				.map(String::valueOf)
				.collect(Collectors.joining(", "))
		);
		
		return warning(msg, entities, null);
	}
	
}
