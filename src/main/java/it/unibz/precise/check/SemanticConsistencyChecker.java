package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.ConsistencyWarning.TaskLocation;
import it.unibz.precise.check.ModelToGraphTranslator.EdgeMode;
import it.unibz.precise.graph.AcyclicOrientationFinder;
import it.unibz.precise.graph.AcyclicOrientationFinder.Result;
import it.unibz.precise.graph.Graph;
import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Location;
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
public class SemanticConsistencyChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "semantic";
	
	public static final String WARNING_MESSAGE_CYCLE = "There is a cycle at the task-unit-level";
	public static final String WARNING_MESSAGE_EDGE = "It is not possible to execute these task-unit combinations without introducing a cycle.";
	
	@Autowired
	private ModelToGraphTranslator translator;
	
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

	@Override
	public Stream<ConsistencyWarning> check(Model model) {
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask = translator.nodesByLocationByTask(model.getTasks());
		Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode = taskLocationByNode(nodesByLocationByTask);
		// N.B. we need disjunctive edges also for whole graph to make sure that each set of nodes connected
		// by disjunctive edges is contained in one single SCC
		DisjunctiveGraph<TaskUnitNode> wholeGraph = translator.translate(nodesByLocationByTask, EdgeMode.IGNORE_SIMPLE);
		
		// TODO: decide what to do with the following
		//removeSimpleBridges(wholeGraph);
		
		return sccFinder.findNonTrivialSCCs(asUndirectedGraph(wholeGraph))				// Divide the graph into independent sub-graphs
			.map(nodes -> nodesByLocationByTask(nodes, taskLocationByNode))				// Compute nodes of those sub-graphs
			.map(nodeMap -> translator.translate(nodeMap, EdgeMode.IGNORE_SIMPLE))		// Compute graphs, ignoring simple edges which never introduce cycles
			.map(AcyclicOrientationFinder<TaskUnitNode>::new)
			.map(AcyclicOrientationFinder::search)										
			.flatMap(result -> warnings(result, taskLocationByNode));
	}
	
//	/**
//	 * Removes bridges that do not introduce problems in the given graph.
//	 * The two nodes connected by such bridges must not be contained in the same exclusive group
//	 * of a disjunctive edge.
//	 */
//	private void removeSimpleBridges(DisjunctiveGraph<TaskUnitNode> wholeGraph) {
//		Map<TaskUnitNode, List<TaskUnitNode>> bridges = new BridgeFinder<>(asUndirectedGraph(wholeGraph)).search();
//		bridges.forEach((node, neighbors) -> {
//			Set<TaskUnitNode> succs = wholeGraph.successorSet(node);
//			Set<DisjunctiveEdge<TaskUnitNode>> disj = wholeGraph.disjunctions(node);
//			neighbors.stream()
//				.filter(n -> disj.stream().anyMatch(edge -> edge.getSide(node).contains(n)))
//				.map(n -> succs.contains(n) ? new Arc<>(node, n) : new Arc<>(n, node))
//				.forEach(wholeGraph::removeArc);
//		});
//	}
	
	/** Produces warnings for the given result if it represents an error. */
	private Stream<ConsistencyWarning> warnings(Result<TaskUnitNode> result, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode) {
		List<List<TaskUnitNode>> sccs = result.getSccs();
		if (sccs != null)
			return sccs.stream().map(nodes -> cycleWarning(nodes, taskLocationByNode));
		DisjunctiveEdge<TaskUnitNode> edge = result.getProblematicEdge();
		if (edge != null)
			return Stream.of(edgeWarning(edge, taskLocationByNode));
		return Stream.empty();
	}
	
	/** Produces a warning describing a cycle among the given nodes. */
	private ConsistencyWarning cycleWarning(List<TaskUnitNode> cycleNodes, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode) {
		List<TaskLocation> taskLocations = cycleNodes.stream()
			.map(taskLocationByNode::get)
			.flatMap(List::stream)
			.collect(Collectors.toList());
		Set<Task> involvedTasks = taskLocations.stream()
			.map(TaskLocation::getTask)
			.collect(Collectors.toSet());
		List<Dependency> dependencies = CheckerUtil.restrictDependenciesByTasks(involvedTasks).collect(Collectors.toList());
			
		return warning(WARNING_MESSAGE_CYCLE, dependencies, taskLocations);
	}
	
	/** Produces a warning describing that the given edge cannot be resolved in any direction. */
	private ConsistencyWarning edgeWarning(DisjunctiveEdge<TaskUnitNode> problematicEdge, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode) {
		List<TaskLocation> taskLocations = Stream.of(
			problematicEdge.getLeft(),
			problematicEdge.getRight()
		)
		.flatMap(Set::stream)
		.map(taskLocationByNode::get)
		.flatMap(List::stream)
		.collect(Collectors.toList());
		
		return warning(WARNING_MESSAGE_EDGE, null, taskLocations);
	}
	
	/**
	 * Inverse the given map, by mapping each contained node to all {@link TaskLocation}s that produced it.
	 * A node is mapped to multiple {@link TaskLocation}s iff those locations are overlapping.
	 */
	private Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode(Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask) {
		Map<TaskUnitNode, List<TaskLocation>> res = new HashMap<>();
		for (Entry<Task, List<List<TaskUnitNode>>> e : nodesByLocationByTask.entrySet()) {
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
	
	/** Collect the given nodes by task and location as expected by {@link ModelToGraphTranslator#translate(Map, EdgeMode)}. */
	private Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask(Collection<TaskUnitNode> nodes, Map<TaskUnitNode, List<TaskLocation>> taskLocationByNode) {
		Map<Task, List<List<TaskUnitNode>>> res = new HashMap<>();
		for (TaskUnitNode node : nodes) {
			for (TaskLocation tl : taskLocationByNode.get(node)) {
				Task task = tl.getTask();
				List<Location> locs = task.getLocations();
				int locCount = locs.size();
				// A List does not allow to add an element at a specified index if it is greater than
				// the list's size, so we initialize it with an empty list for each location.
				res.computeIfAbsent(
					task,
					k -> Stream.generate(() -> new ArrayList<TaskUnitNode>())
						.limit(locCount)
						.collect(Collectors.toList())
				).get(tl.getIndex()).add(node);
			}
		}
		return res;
	}
	
	/**
	 * Returns a view of the given graph that considers arcs to be undirected,
	 * and where each side of a {@link DisjunctiveEdge} represents a clique.
	 * Then, for each {@link DisjunctiveEdge} in the original graph, its two
	 * sets of nodes are either contained all within the same or in two separate
	 * strongly connected components.
	 */
	private <T> Graph<T> asUndirectedGraph(DisjunctiveGraph<T> disjunctiveGraph) {
		return new Graph<T>() {
			@Override
			public Collection<T> nodes() {
				return disjunctiveGraph.nodes();
			}
			@Override
			public Stream<T> successors(T node) {
				// Groups of nodes corresponding to the side of disjunctive edges that contain
				// the given node.
				Stream<Set<T>> exclusiveGroups = disjunctiveGraph.disjunctions(node).stream()
					.map(e -> e.getSide(node))
					.filter(Objects::nonNull);		// Should not be necessary
				
				return Stream.concat(
					Stream.of(
						disjunctiveGraph.successorSet(node),
						disjunctiveGraph.predecessorSet(node)
					),
					exclusiveGroups
				).flatMap(Set::stream);
			}
		};
	}
	
}
