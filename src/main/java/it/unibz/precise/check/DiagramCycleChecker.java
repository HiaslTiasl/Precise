package it.unibz.precise.check;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibz.precise.graph.MaterializedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.util.Util;

/**
 * Checks a process models for cycles at the diagram level.
 * 
 * @author MatthiasP
 * @see DiagramGraph
 *
 */
@Service
public class DiagramCycleChecker implements ProblemChecker {
	
	public static final String PROBLEM_TYPE = "cycles";
	
	public static final String PROBLEM_CYCLE_MESSAGE = "The following tasks contain cycles: {0}";
	public static final String PROBLEM_SELFLOOP_MESSAGE = "There is a self-loop on task {0}";

	@Autowired
	private SCCFinder sccFinder;
	
	@Override
	public Category getCategory() {
		return Category.STRUCTURE_WARNING;
	}
	
	@Override
	public String getType() {
		return PROBLEM_TYPE;
	}
	
	/**
	 * Find non-trivial SCCs in the diagram of {@code model} and return a {@link ModelProblem}
	 * for each of them.
	 */
	@Override
	public Stream<ModelProblem> check(Model model) {
		MaterializedGraph graph = DiagramGraph.of(model);
		return sccFinder.findNonTrivialSCCs(graph)
			.map(scc -> checkSCC(model, scc));
	}
	
	/** Produce a warning iff the given SCC actually contains any arcs. */
	private ModelProblem checkSCC(Model model, BitSet scc) {
		ModelProblem p = null;
		int size = Util.size(scc);
		if (size > 1)
			p = cycleWarning(model, scc);
		else if (size == 1) {						// Should always be the case
			Task t = model.getTasks().get(scc.nextSetBit(0));
			p = t.getOut().stream()
				.filter(Dependency::isSelfLoop)
				.filter(Dependency::isPrecedence)
				.findAny()
				.map(d -> selfLoopWarning(model, d))
				.orElse(null);						// Should never be the case
		}
		return p;
	}
	
	/** Produce a warning about the given self-loop. */
	private ModelProblem selfLoopWarning(Model model, Dependency dependency) {
		Task task = dependency.getSource();
		return warning(
			MessageFormat.format(PROBLEM_SELFLOOP_MESSAGE, task.getShortIdentification()),
			Arrays.asList(task, dependency),
			null
		);
	}
	
	
	/** Produce a warning about the given SCC. */
	private ModelProblem cycleWarning(Model model, BitSet scc) {
		// Tasks and dependencies in the given strongly connected component
		List<BaseEntity> entities = CheckerUtil.restrictDiagramByTasks(model, scc).collect(Collectors.toList());
		
		// Create message
		String msg = MessageFormat.format(PROBLEM_CYCLE_MESSAGE,
			scc.stream()
				.mapToObj(model.getTasks()::get)
				.sorted(Task.shortIdentificationComparator())
				.map(Task::getShortIdentification)
				.map(String::valueOf)
				.collect(Collectors.joining(", "))
		);
		
		return warning(msg, entities, null);
	}
	
}
