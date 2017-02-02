package it.unibz.precise.check;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.ModelToGraphTranslator.EdgeMode;
import it.unibz.precise.graph.AcyclicOrientationFinder;
import it.unibz.precise.graph.AcyclicOrientationFinder.Result;
import it.unibz.precise.graph.Graph;
import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.BaseEntity;
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
		// First divide the diagram into independent sub-diagrams
//		DiagramGraph diaGraph = DiagramGraph.undirected(model);
//		List<List<Task>> ccs = sccFinder.findSCCs(diaGraph);
//		
//		System.out.println("Found " + ccs.size() + " connected components at diagram level");
//		
//		return ccs.stream()
//			.filter(SCCFinder::isNonTrivialComponent)
//			.flatMap(this::checkIndependentSubDiagram);
		
		// TODO: Check whether using the above approach or the one below.
		// Splitting at the diagram level might not be a good idea because:
		// - it depends on the assumption that there are no overlapping locations,
		// - it probably does not improve performance wrt. directly partitioning at the task-unit level,
		//   since this step is linear anyway.
		return checkIndependentSubDiagram(model.getTasks());

	}
	
	/** Check the given (sub-) diagram. */
	private Stream<ConsistencyWarning> checkIndependentSubDiagram(List<Task> tasks) {
		// Divide the graph into independent sub-graphs
		// For that purpose we can ignore disjunctive edges
		Graph<TaskUnitNode> undirectedNodeGraph = translator.translate(tasks, EdgeMode.IGNORE_ALL).asUndirectedGraph();
		return sccFinder.findSCCs(undirectedNodeGraph).stream()
			.filter(SCCFinder::isNonTrivialComponent)
			.flatMap(this::checkIndependentSubGraph);
	}
	
	/** Searches an acyclic orientation of the {@link DisjunctiveGraph} corresponding to the given nodes. */
	private Stream<ConsistencyWarning> checkIndependentSubGraph(Collection<TaskUnitNode> nodes) {
		Map<Task, List<TaskUnitNode>> nodesByTask = nodes.stream().collect(Collectors.groupingBy(TaskUnitNode::getTask));
		// We can ignore simple edges because they never introduce cycles
		// and thus do not change the existence of an acyclic orientation
		DisjunctiveGraph<TaskUnitNode> disjGraph = translator.translate(nodesByTask, EdgeMode.IGNORE_SIMPLE);
		System.out.println("Translated to graph:");
		disjGraph.print();
		Result<TaskUnitNode> result = new AcyclicOrientationFinder<>(disjGraph).search();
		return warnings(result);
	}
	
	/** Produces warnings for the given result if it represents an error. */
	private Stream<ConsistencyWarning> warnings(Result<TaskUnitNode> result) {
		List<List<TaskUnitNode>> sccs = result.getSccs();
		if (sccs != null)
			return sccs.stream().map(this::cycleWarning);
		DisjunctiveEdge<TaskUnitNode> edge = result.getProblematicEdge();
		if (edge != null)
			return Stream.of(this.edgeWarning(edge));
		return Stream.empty();
	}
	
	/** Produces a warning describing a cycle among the given nodes. */
	private ConsistencyWarning cycleWarning(List<TaskUnitNode> cycleNodes) {
		Set<Task> involvedTasks = cycleNodes.stream().map(TaskUnitNode::getTask).collect(Collectors.toSet());
		List<BaseEntity> entities = CheckerUtil.restrictDiagramByTasks(involvedTasks).collect(Collectors.toList());
		return warning(WARNING_MESSAGE_CYCLE, entities, null);
	}
	
	/** Produces a warning describing that the given edge cannot be resolved in any direction. */
	private ConsistencyWarning edgeWarning(DisjunctiveEdge<TaskUnitNode> problematicEdge) {
		Set<Task> involvedTasks = Stream.of(
			problematicEdge.getLeft(),
			problematicEdge.getRight()
		)
		.flatMap(Set::stream)
		.map(TaskUnitNode::getTask)
		.collect(Collectors.toSet());
		
		return warning(WARNING_MESSAGE_EDGE, involvedTasks, null);
	}
	
}
