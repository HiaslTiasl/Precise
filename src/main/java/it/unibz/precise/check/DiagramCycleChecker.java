package it.unibz.precise.check;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.model.BaseEntity;
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
		return sccFinder.findNonTrivialSCCs(DiagramGraph.directed(model)).map(this::warning);
	}
	
	/** Produce a warning about the given SCC. */
	private ConsistencyWarning warning(List<Task> scc) {
		// Tasks and dependencies in the given strongly connected component
		List<BaseEntity> entities = CheckerUtil.restrictDiagramByTasks(scc).collect(Collectors.toList());
		
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
