package it.unibz.precise.check;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.ModelProblem.TaskLocation;
import it.unibz.precise.graph.disj.AcyclicOrientationFinder;
import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.graph.disj.OrientationResult;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

/**
 * Checks consistency of a process model at the task-unit level.
 * Translates the model into a {@link DisjunctiveGraph}, and checks whether
 * the resulting graph has an acyclic orientation.
 * The graph is first partitioned into (weakly) connected components,
 * and an acyclic orientation for each of these sub-graphs is searched.
 * 
 * @author MatthiasP
 * @see DisjunctiveGraph
 * @see AcyclicOrientationFinder
 * @see ModelToGraphTranslator
 *
 */
@Service
public class ConsistencyProblemChecker implements ProblemChecker {
	
	public static final String PROBLEM_TYPE = "consistency";
	
	public static final String PROBLEM_MESSAGE_CYCLE = "There is a cycle at the task-unit-level";
	public static final String PROBLEM_MESSAGE_DEADLOCK = "There is a deadlock at the task-unit-level.";
	
	private final Log logger = LogFactory.getLog(ConsistencyProblemChecker.class);
	
	@Autowired
	private ModelToGraphTranslator translator;
	
	@Autowired
	private AcyclicOrientationFinder orientationFinder;

	@Override
	public Category getCategory() {
		return Category.CONSISTENCY_ERROR;
	}

	@Override
	public String getType() {
		return PROBLEM_TYPE;
	}

	@Override
	public Stream<ModelProblem> check(Model model) {
		long t0 = System.nanoTime();
		
		ModelToGraphTranslator.Input input = new ModelToGraphTranslator.Input(model);
		Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode = taskLocationByNode(input);
		DisjunctiveGraph graph = translator.translate(input, true);
		
		long t1 = System.nanoTime();
		
		OrientationResult res = orientationFinder.search(graph);
		
		long t2 = System.nanoTime();
		
		long translationTime = (t1 - t0) / 1000000;
		long algorithmTime = (t2 - t1) / 1000000;
		long totalTime = translationTime + algorithmTime;
		
        if (logger.isInfoEnabled())
        	logger.info("Checks completed in " + totalTime + " ms (Graph construction: " + translationTime + " ms; Algorithm: " + algorithmTime + " ms)");
		
		return res.failureReasons()
			.flatMap(result -> warnings(input, result, taskLocationByNode));
	}
	
	/** Produces warnings for the given result if it represents an error. */
	private Stream<ModelProblem> warnings(ModelToGraphTranslator.Input input,
										  OrientationResult.Leaf result, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode)
	{
		List<BitSet> sccs = result.getSccs();
		if (sccs != null)
			return sccs.stream().map(nodes -> cycleWarning(input, result, nodes, taskLocationByNode));
		DisjunctiveEdge edge = result.getDeadlockEdge();
		if (edge != null)
			return Stream.of(deadlockWarning(input, result, edge, taskLocationByNode));
		return Stream.empty();
	}
	
	/** Produces a warning describing a cycle among the given nodes. */
	private ModelProblem cycleWarning(ModelToGraphTranslator.Input input, OrientationResult.Leaf result,
		  BitSet scc, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode)
	{
		List<TaskLocation> taskLocations = scc.stream()
			.map(result.getGraph()::toOriginalNode)
			.mapToObj(input::nodeAt)
			.map(taskLocationByNode::get)
			.flatMap(List::stream)
			.collect(Collectors.toList());
		Set<Task> involvedTasks = taskLocations.stream()
			.map(TaskLocation::getTask)
			.collect(Collectors.toSet());
		List<Dependency> dependencies = CheckerUtil.restrictDependenciesByTasks(involvedTasks).collect(Collectors.toList());
			
		return warning(PROBLEM_MESSAGE_CYCLE, dependencies, taskLocations);
	}
	
	/** Produces a warning describing that the given edge cannot be resolved in any direction. */
	private ModelProblem deadlockWarning(ModelToGraphTranslator.Input input, OrientationResult.Leaf result,
		DisjunctiveEdge deadlockEdge, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode)
	{
		List<TaskLocation> taskLocations = Stream.of(
			deadlockEdge.getLeft(),
			deadlockEdge.getRight()
		)
		.flatMapToInt(BitSet::stream)
		.map(result.getGraph()::toOriginalNode)
		.mapToObj(n -> taskLocationByNode.get(input.nodeAt(n)))
		.flatMap(List::stream)
		.collect(Collectors.toList());
		
		return warning(PROBLEM_MESSAGE_DEADLOCK, null, taskLocations);
	}
	
	/**
	 * Inverse the given map, by mapping each contained node to all {@link TaskLocation}s that produced it.
	 * A node is mapped to multiple {@link TaskLocation}s iff those locations are overlapping.
	 */
	private Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode(ModelToGraphTranslator.Input input) {
		Map<TaskUnitNode, List<TaskLocation>> res = new HashMap<>();
		for (Entry<Task, List<List<TaskUnitNode>>> e : input.getNodesByLocationByTask().entrySet()) {
			Task task = e.getKey();
			List<List<TaskUnitNode>> locations = e.getValue();
			int locCount = locations.size();
			// Need the indices to create TaskLocations
			for (int i = 0; i < locCount; i++) {
				TaskLocation tl = new TaskLocation(task, i);
				for (TaskUnitNode n : locations.get(i)) {
					res.computeIfAbsent(n, k -> new ArrayList<>())
						.add(tl);
				}
			}
		}
		return res;
	}
	
}
