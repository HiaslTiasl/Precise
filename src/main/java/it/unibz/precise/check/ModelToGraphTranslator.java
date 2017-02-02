package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

/**
 * Translates a {@link Model} to a {@link DisjunctiveGraph} of {@link TaskUnitNode}s.
 * Provides a {@link #translate(Model)} method with several overloads that allow
 * for more control.
 * 
 * @author MatthiasP
 *
 */
@Service
public class ModelToGraphTranslator {
	
	/** Specifies how to handle {@link DisjunctiveEdge}. */
	public enum EdgeMode {
		IGNORE_ALL,
		IGNORE_SIMPLE,
		CONSIDER_ALL
	}
	
	/** Translates the given model to a {@link DisjunctiveGraph} considering all tasks. */
	public DisjunctiveGraph<TaskUnitNode> translate(Model model) {
		return translate(model.getTasks());
	}
	
	/** Translates the given tasks to a {@link DisjunctiveGraph} . */
	public DisjunctiveGraph<TaskUnitNode> translate(List<Task> tasks) {
		return translate(nodesByTask(tasks), EdgeMode.CONSIDER_ALL);
	}

	/** Translates the given tasks to a {@link DisjunctiveGraph}, using the specified {@link EdgeMode}. */
	public DisjunctiveGraph<TaskUnitNode> translate(List<Task> tasks, EdgeMode edgeMode) {
		return translate(nodesByTask(tasks), edgeMode);
	}
	
	/** Processes the map of tasks to nodes and creates a corresponding {@link DisjunctiveGraph}. */
	public DisjunctiveGraph<TaskUnitNode> translate(Map<Task, List<TaskUnitNode>> nodesByTask, EdgeMode edgeMode) {
		DisjunctiveGraph<TaskUnitNode> graph = new DisjunctiveGraph<>();
		
		Set<Task> tasks = nodesByTask.keySet();
		
		for (Task t : tasks) {
			List<TaskUnitNode> nodes = nodesByTask.get(t);
			// Do not consider task without nodes
			if (nodes != null && !nodes.isEmpty()) {
				processTask(graph, t, nodesByTask, nodes, edgeMode);
				for (Dependency dep : t.getOut()) {
					Task target = dep.getTarget();
					List<TaskUnitNode> targetNodes = target == null ? null : nodesByTask.get(target);
					if (targetNodes != null && !targetNodes.isEmpty())
						processDependency(graph, dep, nodesByTask, nodes, targetNodes, edgeMode);
				}
			}
		}
		
		return graph;
	}
	
	//-------------------------------------------
	// Methods for processing diagram elements
	//-------------------------------------------
	
	/** Processes the given task. */
	private void processTask(
		DisjunctiveGraph<TaskUnitNode> graph,
		Task task,
		Map<Task, List<TaskUnitNode>> nodesByTask,
		List<TaskUnitNode> nodes,
		EdgeMode edgeMode
	) {
		graph.addAllNodes(nodes);
		processOrdering(graph, task.getOrderSpecifications(), 0, nodes);
		if (edgeMode != EdgeMode.IGNORE_ALL)
			processExclusiveness(graph, nodesByTask, task, edgeMode);
	}
	
	/** Processes the given dependency. */
	private void processDependency(
		DisjunctiveGraph<TaskUnitNode> graph,
		Dependency dep,
		Map<Task, List<TaskUnitNode>> nodesByTask,
		List<TaskUnitNode> sourceNodes,
		List<TaskUnitNode> targetNodes,
		EdgeMode edgeMode
	) {
		Scope scope = dep.getScope();
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups = groupNodesBy(sourceNodes, scope);
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups = groupNodesBy(targetNodes, scope);
		if (sourceGroups != null && targetGroups != null) {
			processBasicPrecedence(graph, sourceGroups, targetGroups);
			boolean alt = dep.isAlternate();
			boolean chain = dep.isChain();
			if (edgeMode != EdgeMode.IGNORE_ALL && (alt || chain)) {
				// N.B. exclusive groups for dependencies always contain at least 2 nodes,
				// so no need to consider IGNORE_SINGLE here.
				Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = exclusiveGroups(sourceGroups, targetGroups);
				if (alt)
					processAlternatePrecedence(graph, exclusiveGroups);
				if (chain)
					processChainPrecedence(graph, dep, nodesByTask, exclusiveGroups);
			}
		}
	}
	
	
	/** Recursively processes the given ordering specifications of the given nodes, starting at index {@code i}. */
	private void processOrdering(DisjunctiveGraph<TaskUnitNode> graph, List<OrderSpecification> orderSpecs, int i, Collection<TaskUnitNode> nodes) {
		if (i >= orderSpecs.size())
			return;
		OrderSpecification os = orderSpecs.get(i);
		OrderType ot = os.getOrderType();
		int next = i + 1;
		
		if (ot == OrderType.NONE)
			processOrdering(graph, orderSpecs, next, nodes);	// NONE -> ignore this specification
		else {
			Attribute attr = os.getAttribute();
			// Partition nodes according to this attribute
			Map<String, List<TaskUnitNode>> groups = nodes.stream().collect(Collectors.groupingBy(n -> nodeAttributeValue(n, attr)));
			List<TaskUnitNode> prev = null;			// Previous group
			for (String val : attr.getRange()) {
				List<TaskUnitNode> g = groups.get(val);
				if (g != null) {
					processOrdering(graph, orderSpecs, next, g);		// Recursion from the current partition
					if (prev!= null) {
						// Add arcs between consecutive groups, in the appropriate direction, if any
						switch (ot) {
						case ASCENDING:
							graph.addAllArcs(prev, g);
							break;
						case DESCENDING:
							graph.addAllArcs(g, prev);
							break;
						default:	// PARALLEL
							break;
						}
					}
					prev = g;
				}
			}
		}
	}
	
	/** Processes the exclusiveness of task {@code t}. */
	private void processExclusiveness(DisjunctiveGraph<TaskUnitNode> graph, Map<Task, List<TaskUnitNode>> nodesByTask, Task t, EdgeMode edgeMode) {
		Scope exclusiveness = t.getExclusiveness();
		// Partition the nodes of the task by the projection to the scope of its exclusiveness
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = groupNodesBy(nodesByTask.get(t), exclusiveness);
		// Add a disjunctive edge from exclusive groups to nodes of other tasks and the same projection
		nodesByTask.entrySet().stream()
			.filter(e -> !t.equals(e.getKey()))
			.map(Map.Entry::getValue)
			.flatMap(List::stream)
			.map(n -> {
				// Lookup projection in exclusive groups
				// Return resulting disjunctive edge or null
				Set<TaskUnitNode> matchRes = exclusiveGroups.get(exclusiveness.project(n.getUnit()));
				return matchRes == null || (edgeMode == EdgeMode.IGNORE_SIMPLE && matchRes.size() == 1) ? null
					: new DisjunctiveEdge<>(Collections.singleton(n), matchRes);
			})
			.filter(Objects::nonNull)
			.forEach(graph::addEdge);
	}
	
	/** Processes the given dependency as a basic precedence. */
	private void processBasicPrecedence(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups
	) {
		for (Entry<Map<Attribute, String>, Set<TaskUnitNode>> g : sourceGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			Set<TaskUnitNode> sourceNodes = g.getValue();
			Set<TaskUnitNode> targetNodes = targetGroups.get(projection);
			if (targetNodes != null)
				graph.addAllArcs(sourceNodes, targetNodes);
		}
	}
	
	/** Processes an alternate precedence that has the given exclusive groups*/
	private void processAlternatePrecedence(DisjunctiveGraph<TaskUnitNode> graph, Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups) {
		// Alternate precedences have disjunctive edges among pairs of their own exclusive groups.
		// Using an auxiliary list for index-based iteration, which is useful to avoid looking twice at the same combination.
		List<Set<TaskUnitNode>> valueList = new ArrayList<>(exclusiveGroups.values());
		int len = valueList.size();
		for (int i = 0; i < len - 1; i++) {
			Set<TaskUnitNode> left = valueList.get(i);
			for (int j = i + 1; j < len; j++)			// Start at next element
				graph.addEdge(new DisjunctiveEdge<>(left, valueList.get(j)));
		}
	}
	
	/** Processes the given dependency as a chain precedence. */
	private void processChainPrecedence(
		DisjunctiveGraph<TaskUnitNode> graph,
		Dependency dep,
		Map<Task, List<TaskUnitNode>> nodesByTask,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups
	) {
		Task source = dep.getSource();
		Task target = dep.getTarget();
		Scope scope = dep.getScope();
		// Add a disjunctive edge from every node of other tasks to exclusive groups of matching projections to given scope
		nodesByTask.entrySet().stream()
			.filter(e -> !source.equals(e.getKey()) && !target.equals(e.getKey()))
			.map(Entry::getValue)
			.flatMap(List::stream)
			.map(n -> {
				// Match to exclusive group by projection
				Map<Attribute, String> p = scope.project(n.getUnit());
				Set<TaskUnitNode> nodes = exclusiveGroups.get(p);
				return nodes == null ? null
					: new DisjunctiveEdge<TaskUnitNode>(Collections.singleton(n), nodes);
			})
			.filter(Objects::nonNull)
			.forEach(graph::addEdge);
	}
	
	//-------------------------------------------
	// Helper methods
	//-------------------------------------------
	
	/** Returns the exclusive groups of a dependency with the given source and target groups. */
	private Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups(
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups
	) {
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = new HashMap<>();
		Map<Map<Attribute, String>, Set<TaskUnitNode>> iterateGroups = sourceGroups.size() < targetGroups.size() ? sourceGroups : targetGroups;
		Map<Map<Attribute, String>, Set<TaskUnitNode>> otherGroups = iterateGroups == sourceGroups ? targetGroups : sourceGroups;
		for (Entry<Map<Attribute, String>, Set<TaskUnitNode>> g : iterateGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			Set<TaskUnitNode> otherNodes = otherGroups.get(projection);
			if (otherNodes != null) {
				// Merge source and target nodes
				Set<TaskUnitNode> exclusiveNodes = Stream.of(g.getValue(), otherNodes)
					.flatMap(Set::stream)
					.collect(Collectors.toSet());
				exclusiveGroups.put(projection, exclusiveNodes);
			}
		}
		return exclusiveGroups;
	}
	
	/** Returns a map of the given tasks to the corresponding {@link TaskUnitNode}s. */
	private Map<Task, List<TaskUnitNode>> nodesByTask(List<Task> tasks) {
		return tasks.stream()
			.collect(Collectors.toMap(
				Function.identity(),
				t -> t.getLocations().stream()
					.flatMap(l -> units(t, l))
					.map(ahn -> new TaskUnitNode(t, ahn))
					.collect(Collectors.toList())
				)
			);
	}
	
	/** Returns a stream of all {@link AttributeHierarchyNode}s at the unit level of the given task and location. */
	private Stream<AttributeHierarchyNode> units(Task t, Location l) {
		Stream<AttributeHierarchyNode> res;
		AttributeHierarchyNode ahn = l.getNode();
		if (ahn != null)
			res = ahn.unitsStream();
		else {
			// A global location -> return all units contained in the phase, if any
			TaskType type = t.getType();
			Phase phase = type == null ? null : type.getPhase();
			res = phase == null ? Stream.empty() : phase.unitsStream();
		}
		return res; 
	}
	
	/** Groups the given nodes by projecting them to the given scope. */
	private Map<Map<Attribute, String>, Set<TaskUnitNode>> groupNodesBy(Collection<TaskUnitNode> nodes, Scope scope) {
		return nodes == null ? null 
			: nodes.stream()
				.collect(Collectors.groupingBy(n -> scope.project(n.getUnit()), Collectors.toSet()));
	}
	
	
	/** Returns the value of the given node corresponding to the given attribute. */
	private String nodeAttributeValue(TaskUnitNode node, Attribute attribute) {
		return node.getUnit().ancestorStream()
			.filter(n -> n.getLevel().getAttribute().equals(attribute))
			.findFirst()
			.map(AttributeHierarchyNode::getValue)
			.orElse(null);
	}

}
