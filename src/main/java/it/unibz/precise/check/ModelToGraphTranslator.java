package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Activity;
import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Scope.Type;
import it.unibz.precise.model.Task;
import it.unibz.util.Util;

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
	
	/** Translates the given model to a {@link DisjunctiveGraph} considering all tasks. */
	public DisjunctiveGraph<TaskUnitNode> translate(Model model) {
		return translate(model.getTasks());
	}
	
	/** Translates the given tasks to a {@link DisjunctiveGraph} . */
	public DisjunctiveGraph<TaskUnitNode> translate(List<Task> tasks) {
		return translate(nodesByLocationByTask(tasks), false);
	}

	/** Translates the given tasks to a {@link DisjunctiveGraph}, ignoring simple edges if requested. */
	public DisjunctiveGraph<TaskUnitNode> translate(List<Task> tasks, boolean ignoreSimpleEdges) {
		return translate(nodesByLocationByTask(tasks), ignoreSimpleEdges);
	}
	
	/**
	 * Processes the map of tasks to nodes and creates a corresponding {@link DisjunctiveGraph},
	 * ignoring simple edges if requested.
	 */
	public DisjunctiveGraph<TaskUnitNode> translate(Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask, boolean ignoreSimpleEdges) {
		DisjunctiveGraph<TaskUnitNode> graph = new DisjunctiveGraph<>();
		
		// N.B: we can only add arcs and edges between existing nodes,
		// so we need to add all nodes first.
		addNodes(graph, nodesByLocationByTask);
		addArcsAndEdges(graph, nodesByLocationByTask, ignoreSimpleEdges);
		
		return graph;
	}
	
	/** Adds all nodes in the given map to the given graph. */
	public void addNodes(DisjunctiveGraph<TaskUnitNode> graph, Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask) {
		nodesByLocationByTask.values().stream()
			.flatMap(List::stream)
			.forEach(graph::addAllNodes);
	}
	
	/**
	 * Processes the map of tasks to nodes and adds corresponding arcs and edges to the given task,
	 * ignoring simple edges if requested.
	 * A simple edge is an edge between two singletons.
	 */
	public void addArcsAndEdges(DisjunctiveGraph<TaskUnitNode> graph, Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask, boolean ignoreSimpleEdges) {
		Set<Task> tasks = nodesByLocationByTask.keySet();
		for (Task t : tasks) {
			List<List<TaskUnitNode>> locations = nodesByLocationByTask.get(t);
			// Do not consider task without nodes
			if (locations != null && !locations.isEmpty()) {
				addTaskArcsAndEdges(graph, t, nodesByLocationByTask, locations, ignoreSimpleEdges);
				for (Dependency dep : t.getOut()) {
					Task target = dep.getTarget();
					List<List<TaskUnitNode>> targetLocations = target == null ? null : nodesByLocationByTask.get(target);
					if (targetLocations != null && !targetLocations.isEmpty())
						addDependencyArcsAndEdges(graph, dep, nodesByLocationByTask, locations, targetLocations, ignoreSimpleEdges);
				}
			}
		}
	}
	
	//-------------------------------------------
	// Methods for processing diagram elements
	//-------------------------------------------
	
	/**
	 * Processes the given task and adds corresponding arcs and edges to the given graph,
	 * ignoring simple edges if requested.
	 */
	private void addTaskArcsAndEdges(
		DisjunctiveGraph<TaskUnitNode> graph,
		Task task,
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationTask,
		List<List<TaskUnitNode>> locations,
		boolean ignoreSimpleEdges
	) {
		List<TaskUnitNode> allNodes = locations.stream()
			.flatMap(List::stream)
			.collect(Collectors.toList());
		
		processOrdering(graph, task.getOrderSpecifications(), 0, allNodes);
		processExclusiveness(graph, nodesByLocationTask, task, ignoreSimpleEdges);
	}
	
	/**
	 * Processes the given dependency and adds corresponding arcs and edges to the given graph.
	 * Dependencies never introduce singleton edges, so all edges are considered.
	 */
	private void addDependencyArcsAndEdges(
		DisjunctiveGraph<TaskUnitNode> graph,
		Dependency dep,
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask,
		List<List<TaskUnitNode>> sourceLocations,
		List<List<TaskUnitNode>> targetLocations,
		boolean ignoreSimpleEdges
	) {
		Scope scope = dep.getScope();
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups = groupNodesBy(sourceLocations, scope);
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups = groupNodesBy(targetLocations, scope);
		if (sourceGroups != null && targetGroups != null) {
			if (dep.isPrecedence())
				processBasicPrecedence(graph, sourceGroups, targetGroups);
			boolean alt = dep.isAlternate();
			boolean chain = dep.isChain();
			if (alt || chain) {
				Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = sharedExclusiveGroups(graph, sourceGroups, targetGroups);
				if (alt)
					processAlternatePrecedence(graph, sourceGroups, targetGroups, exclusiveGroups);
				if (chain)
					processChainPrecedence(graph, dep, nodesByLocationByTask, exclusiveGroups, sourceGroups, targetGroups, ignoreSimpleEdges);
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
	private void processExclusiveness(DisjunctiveGraph<TaskUnitNode> graph, Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask, Task t, boolean ignoreSimpleEdges) {
		Scope exclusiveness = t.getExclusiveness();
		// Only consider exclusiveness if also unit edges are needed, or the task is actually exclusive.
		if (!ignoreSimpleEdges && exclusiveness.getType() == Type.UNIT) {
			// Partition the nodes of the task by the projection to the scope of its exclusiveness
			Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = groupNodesBy(nodesByLocationByTask.get(t), exclusiveness);
			for (Set<TaskUnitNode> g : exclusiveGroups.values())
				graph.addExclusiveGroup(g);
			// Add a disjunctive edge from exclusive groups to nodes of other tasks and the same projection
			addExclusiveAcces(graph, nodesByLocationByTask, exclusiveGroups, exclusiveness, ignoreSimpleEdges, t);
		}
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
	private void processAlternatePrecedence(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups
	) {
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
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sharedExclusiveGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups,
		boolean ignoreSimpleEdges
	) {
		Task source = dep.getSource();
		Task target = dep.getTarget();
		Scope scope = dep.getScope();
		// Add non-shared exclusive groups
		Map<Map<Attribute, String>, Set<TaskUnitNode>> allExclusiveGroups = allExclusiveGroups(graph, sourceGroups, targetGroups, sharedExclusiveGroups);
		
		// Add a disjunctive edge from every node of other tasks to exclusive groups of matching projections to given scope
		addExclusiveAcces(graph, nodesByLocationByTask, allExclusiveGroups, scope, ignoreSimpleEdges, source, target);
	}
	
	//-------------------------------------------
	// Helper methods
	//-------------------------------------------
	
	/**
	 * Give several tasks exclusive access to construction areas of the given scope
	 * by adding the corresponding {@link DisjunctiveEdge}s.
	 * If requested by means of {@code ignoreSimpleEdges}, simple edges are not added.
	 * A simple edge is an edge between two singletons.
	 */
	private void addExclusiveAcces(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups,
		Scope scope,
		boolean ignoreSimpleEdges,
		Task... tasks
	) {
		nodesByLocationByTask.entrySet().stream()
			.filter(e -> Stream.of(tasks).noneMatch(Predicate.isEqual(e.getKey())))
			.map(Entry::getValue)
			.flatMap(List::stream)
			.flatMap(List::stream)
			.map(n -> {
				// Match exclusive group by projection
				Map<Attribute, String> p = scope.project(n.getUnit());
				Set<TaskUnitNode> nodes = exclusiveGroups.get(p);
				return nodes == null || (ignoreSimpleEdges && nodes.size() == 1) || nodes.contains(n) ? null
					: new DisjunctiveEdge<TaskUnitNode>(Collections.singleton(n), nodes);
			})
			.filter(Objects::nonNull)
			.forEach(graph::addEdge);
	}
	
	/** Returns the shared exclusive groups of a dependency with the given source and target groups. */
	private Map<Map<Attribute, String>, Set<TaskUnitNode>> sharedExclusiveGroups(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups
	) {
		Map<Map<Attribute, String>, Set<TaskUnitNode>> exclusiveGroups = new HashMap<>();
		
		for (Entry<Map<Attribute, String>, Set<TaskUnitNode>> g : sourceGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			Set<TaskUnitNode> sourceNodes = g.getValue();
			Set<TaskUnitNode> targetNodes = targetGroups.get(projection);
			if (Util.hasElements(targetNodes)) {
				// Merge source and target nodes
				Set<TaskUnitNode> exclusiveNodes = new HashSet<>(sourceNodes);
				exclusiveNodes.addAll(targetNodes);
				exclusiveGroups.put(projection, exclusiveNodes);
				graph.addExclusiveGroup(exclusiveNodes);
			}
		}
		return exclusiveGroups;
	}
	
	/** Returns the shared exclusive groups of a dependency with the given source and target groups. */
	private Map<Map<Attribute, String>, Set<TaskUnitNode>> allExclusiveGroups(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sourceGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> targetGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sharedExclusiveGroups
	) {
		Map<Map<Attribute, String>, Set<TaskUnitNode>> allExclusiveGroups = new HashMap<>(sharedExclusiveGroups);
		addNonSharedExclusiveGroups(graph, sourceGroups, sharedExclusiveGroups, allExclusiveGroups);
		addNonSharedExclusiveGroups(graph, targetGroups, sharedExclusiveGroups, allExclusiveGroups);
		return allExclusiveGroups;
	}
	
	private void addNonSharedExclusiveGroups(
		DisjunctiveGraph<TaskUnitNode> graph,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> groups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> sharedExclusiveGroups,
		Map<Map<Attribute, String>, Set<TaskUnitNode>> allExclusiveGroups
		
	) {
		for (Entry<Map<Attribute, String>, Set<TaskUnitNode>> g : groups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			Set<TaskUnitNode> nodes = g.getValue();
			if (Util.hasElements(nodes) && !Util.hasElements(sharedExclusiveGroups.get(projection))) {
				// Merge source and target nodes
				allExclusiveGroups.put(projection, nodes);
				graph.addExclusiveGroup(nodes);
			}
		}
	}
	
	
	/** Returns a map of the given tasks to the corresponding {@link TaskUnitNode}s, grouped by location. */
	public Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask(List<Task> tasks) {
		return tasks.stream()
			.collect(Collectors.toMap(
				Function.identity(),
				t -> {
					List<Location> locs = t.getLocations();
					int locCount = locs.size();
					List<List<TaskUnitNode>> nodeLists = new ArrayList<>(locCount);
					for (int i = 0; i < locCount; i++) {
						Location l = locs.get(i);
						List<TaskUnitNode> nodes = units(t, l)
							.map(ahn -> new TaskUnitNode(t.getActivity(), ahn))
							.collect(Collectors.toList());
						nodeLists.add(nodes);
					}
					return nodeLists;
				})
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
			Activity activity = t.getActivity();
			Phase phase = activity == null ? null : activity.getPhase();
			res = phase == null ? Stream.empty() : phase.unitsStream();
		}
		return res; 
	}
	
	/** Groups the given nodes by projecting them to the given scope. */
	private Map<Map<Attribute, String>, Set<TaskUnitNode>> groupNodesBy(List<List<TaskUnitNode>> locations, Scope scope) {
		return locations == null ? null 
			: locations.stream()
				.flatMap(List::stream)
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
