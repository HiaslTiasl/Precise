package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
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
	
	public static class Input {

		private final Model model;
		private final Map<Task,List<List<TaskUnitNode>>> nodesByLocationByTask;
		
		// Using a sorted array of nodes to map back and forth between TaskUnitNodes and indices.
		// Mapping from indices to nodes is only needed when reporting, whereas mapping from nodes to indices is always needed
		// to translate arcs and edges to the internal Graph representation. While a Map would provide a faster mapping (O(n)), it would
		// need a bit more space and not help at all for the other direction.
		private final TaskUnitNode[] nodesByIndex;
		
		public Input(Model model) {
			this.model = model;
			List<Task> tasks = model.getTasks();
			nodesByLocationByTask = tasks.stream().collect(Collectors.toMap(
				Function.identity(),
				t -> {
					List<Location> locs = t.getLocations();
					List<List<TaskUnitNode>> nodeLists = new ArrayList<>(locs.size());
					for (Location l : locs) {
						nodeLists.add(units(t, l)
							.map(ahn -> new TaskUnitNode(t.getActivity(), ahn))
							.collect(Collectors.toList()));
					}
					return nodeLists;
				}
			));
			nodesByIndex = nodesByLocationByTask.values().stream()
				.flatMap(List::stream)
				.flatMap(List::stream)
				.toArray(TaskUnitNode[]::new);
			Arrays.sort(nodesByIndex, TaskUnitNode.BY_TO_STRING);
		}

		public Model getModel() {
			return model;
		}
		
		public int nodeCount() {
			return nodesByIndex.length;
		}
		
		public Map<Task, List<List<TaskUnitNode>>> getNodesByLocationByTask() {
			return nodesByLocationByTask;
		}
		
		public int indexOf(TaskUnitNode node) {
			return Arrays.binarySearch(nodesByIndex, node, TaskUnitNode.BY_TO_STRING);
		}
		
		public TaskUnitNode nodeAt(int index) {
			return nodesByIndex[index];
		}
	}
	
	/** Translates the given model to a {@link DisjunctiveGraph} considering all tasks. */
	public DisjunctiveGraph translate(Model model) {
		return translate(model, false);
	}

	/** Translates the given model to a {@link DisjunctiveGraph}, ignoring simple edges if requested. */
	public DisjunctiveGraph translate(Model model, boolean ignoreSimpleEdges) {
		return translate(new Input(model), ignoreSimpleEdges);
	}

	/** Translates the given input to a {@link DisjunctiveGraph}, ignoring simple edges if requested. */
	public DisjunctiveGraph translate(Input input) {
		return translate(input, false);
	}
	
	/**
	 * Processes the map of tasks to nodes and creates a corresponding {@link DisjunctiveGraph},
	 * ignoring simple edges if requested.
	 */
	public DisjunctiveGraph translate(Input input, boolean ignoreSimpleEdges) {
		DisjunctiveGraph graph = DisjunctiveGraph.sealedNodes(input.nodeCount());
		addArcsAndEdges(graph, input, ignoreSimpleEdges);
		return graph;
	}
	
	/**
	 * Processes the map of tasks to nodes and adds corresponding arcs and edges to the given task,
	 * ignoring simple edges if requested.
	 * A simple edge is an edge between two singletons.
	 */
	public void addArcsAndEdges(DisjunctiveGraph graph, Input input, boolean ignoreSimpleEdges) {
		Map<Task, List<List<TaskUnitNode>>> nodesByLocationByTask = input.nodesByLocationByTask;
		for (Map.Entry<Task, List<List<TaskUnitNode>>> e : nodesByLocationByTask.entrySet()) {
			List<List<TaskUnitNode>> locations = e.getValue();
			// Do not consider task without nodes
			if (locations != null && !locations.isEmpty()) {
				Task t = e.getKey();
				addTaskArcsAndEdges(graph, t, input, locations, ignoreSimpleEdges);
				for (Dependency dep : t.getOut()) {
					Task target = dep.getTarget();
					List<List<TaskUnitNode>> targetLocations = target == null ? null : nodesByLocationByTask.get(target);
					if (targetLocations != null && !targetLocations.isEmpty())
						addDependencyArcsAndEdges(graph, dep, input, locations, targetLocations, ignoreSimpleEdges);
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
		DisjunctiveGraph graph,
		Task task,
		Input input,
		List<List<TaskUnitNode>> locations,
		boolean ignoreSimpleEdges
	) {
		List<TaskUnitNode> allNodes = locations.stream()
			.flatMap(List::stream)
			.collect(Collectors.toList());
		
		processOrdering(graph, input, task.getOrderSpecifications(), 0, allNodes);
		processExclusiveness(graph, input, task, ignoreSimpleEdges);
	}
	
	/**
	 * Processes the given dependency and adds corresponding arcs and edges to the given graph.
	 * Dependencies never introduce singleton edges, so all edges are considered.
	 */
	private void addDependencyArcsAndEdges(
		DisjunctiveGraph graph,
		Dependency dep,
		Input input,
		List<List<TaskUnitNode>> sourceLocations,
		List<List<TaskUnitNode>> targetLocations,
		boolean ignoreSimpleEdges
	) {
		Scope scope = dep.getScope();
		Map<Map<Attribute, String>, BitSet> sourceGroups = groupNodesBy(input, sourceLocations, scope);
		Map<Map<Attribute, String>, BitSet> targetGroups = groupNodesBy(input, targetLocations, scope);
		if (sourceGroups != null && targetGroups != null) {
			if (dep.isPrecedence())
				processBasicPrecedence(graph, input, sourceGroups, targetGroups);
			boolean alt = dep.isAlternate();
			boolean chain = dep.isChain();
			if (alt || chain) {
				Map<Map<Attribute, String>, BitSet> exclusiveGroups = sharedGroups(sourceGroups, targetGroups);
				if (alt)
					processAlternatePrecedence(graph, input, exclusiveGroups);
				if (chain) {
					addNonSharedGroups(exclusiveGroups, sourceGroups);
					addNonSharedGroups(exclusiveGroups, targetGroups);
					processChainPrecedence(graph, dep, input, exclusiveGroups, ignoreSimpleEdges);
				}
			}
		}
	}
	
	/** Recursively processes the given ordering specifications of the given nodes, starting at index {@code i}. */
	private void processOrdering(DisjunctiveGraph graph, Input input, List<OrderSpecification> orderSpecs, int i, Collection<TaskUnitNode> nodes) {
		if (i >= orderSpecs.size())
			return;
		OrderSpecification os = orderSpecs.get(i);
		OrderType ot = os.getOrderType();
		int next = i + 1;
		
		if (ot == OrderType.NONE)
			processOrdering(graph, input, orderSpecs, next, nodes);	// NONE -> ignore this specification
		else {
			Attribute attr = os.getAttribute();
			// Partition nodes according to this attribute
			Map<String, List<TaskUnitNode>> groups = nodes.stream().collect(Collectors.groupingBy(n -> nodeAttributeValue(n, attr)));
			BitSet prev = null;			// Previous group
			for (String val : attr.getRange()) {
				List<TaskUnitNode> g = groups.get(val);
				if (g != null) {
					BitSet group = toBitSet(input, g);
					processOrdering(graph, input, orderSpecs, next, g);		// Recursion from the current partition
					if (prev!= null) {
						// Add arcs between consecutive groups, in the appropriate direction, if any
						switch (ot) {
						case ASCENDING:
							graph.addAllArcs(prev, group);
							break;
						case DESCENDING:
							graph.addAllArcs(group, prev);
							break;
						default:	// PARALLEL
							break;
						}
					}
					prev = group;
				}
			}
		}
	}
	
	private static BitSet toBitSet(Input input, Collection<TaskUnitNode> nodes) {
		BitSet bitSet = new BitSet(input.nodeCount());
		for (TaskUnitNode n : nodes)
			bitSet.set(input.indexOf(n));
		return bitSet;
	}
	
	/** Processes the exclusiveness of task {@code t}. */
	private void processExclusiveness(DisjunctiveGraph graph, Input input, Task t, boolean ignoreSimpleEdges) {
		Scope exclusiveness = t.getExclusiveness();
		// Only consider exclusiveness if also unit edges are needed, or the task is actually exclusive.
		if (!ignoreSimpleEdges && exclusiveness.getType() == Type.UNIT) {
			// Partition the nodes of the task by the projection to the scope of its exclusiveness
			Map<Map<Attribute, String>, BitSet> exclusiveGroups = groupNodesBy(input, input.nodesByLocationByTask.get(t), exclusiveness);
			// Add a disjunctive edge from exclusive groups to nodes of other tasks and the same projection
			addExclusiveAcces(graph, input, exclusiveGroups, exclusiveness, ignoreSimpleEdges, t);
		}
	}
	
	/** Processes the given dependency as a basic precedence. */
	private void processBasicPrecedence(
		DisjunctiveGraph graph,
		Input input,
		Map<Map<Attribute, String>, BitSet> sourceGroups,
		Map<Map<Attribute, String>, BitSet> targetGroups
	) {
		for (Entry<Map<Attribute, String>, BitSet> g : sourceGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			BitSet sourceNodes = g.getValue();
			BitSet targetNodes = targetGroups.get(projection);
			if (targetNodes != null)
				graph.addAllArcs(sourceNodes, targetNodes);
		}
	}
	
	/** Processes an alternate precedence that has the given exclusive groups. */
	private void processAlternatePrecedence(
		DisjunctiveGraph graph,
		Input input,
		Map<Map<Attribute, String>, BitSet> sharedGroups
	) {
		// Alternate precedences have disjunctive edges among pairs of their own exclusive groups.
		// Using an auxiliary array for index-based iteration, which is useful to avoid looking twice at the same combination.
		BitSet[] groups = sharedGroups.values().toArray(new BitSet[0]);
		
		for (int i = 0; i < groups.length - 1; i++) {
			BitSet left = groups[i];
			for (int j = i + 1; j < groups.length; j++)			// Start at next element
				graph.addEdge(new DisjunctiveEdge(left, groups[j]));
		}
	}
	
	/** Processes the given dependency as a chain precedence. */
	private void processChainPrecedence(
		DisjunctiveGraph graph,
		Dependency dep,
		Input input,
		Map<Map<Attribute, String>, BitSet> allGroups,
		boolean ignoreSimpleEdges
	) {
		Task source = dep.getSource();
		Task target = dep.getTarget();
		Scope scope = dep.getScope();
		// Add a disjunctive edge from every node of other tasks to exclusive groups of matching projections to given scope
		addExclusiveAcces(graph, input, allGroups, scope, ignoreSimpleEdges, source, target);
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
		DisjunctiveGraph graph,
		Input input,
		Map<Map<Attribute, String>, BitSet> exclusiveGroups,
		Scope scope,
		boolean ignoreSimpleEdges,
		Task... tasks
	) {
		input.nodesByLocationByTask.entrySet().stream()
			.filter(e -> Stream.of(tasks).noneMatch(Predicate.isEqual(e.getKey())))
			.map(Entry::getValue)
			.flatMap(List::stream)
			.flatMap(List::stream)
			.map(n -> {
				// Match exclusive group by projection
				Map<Attribute, String> p = scope.project(n.getUnit());
				BitSet nodes = exclusiveGroups.get(p);
				int index = input.indexOf(n);
				if (nodes == null || (ignoreSimpleEdges && nodes.size() == 1) || nodes.get(index))
					return null;
				BitSet src = new BitSet();
				src.set(index);
				return new DisjunctiveEdge(src, nodes);
			})
			.filter(Objects::nonNull)
			.forEach(graph::addEdge);
	}
	
	/** Returns the exclusive groups of a dependency with the given source and target groups. */
	private Map<Map<Attribute, String>, BitSet> sharedGroups(
		Map<Map<Attribute, String>, BitSet> sourceGroups,
		Map<Map<Attribute, String>, BitSet> targetGroups
	) {
		Map<Map<Attribute, String>, BitSet> iterateGroups = sourceGroups.size() < targetGroups.size() ? sourceGroups : targetGroups;
		Map<Map<Attribute, String>, BitSet> otherGroups = iterateGroups == sourceGroups ? targetGroups : sourceGroups;
		
		Map<Map<Attribute, String>, BitSet> exclusiveGroups = new HashMap<>();
		for (Entry<Map<Attribute, String>, BitSet> g : iterateGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			BitSet nodes = g.getValue();
			BitSet otherNodes = otherGroups.get(projection);
			if (nodes != null && !nodes.isEmpty()) {
				if (otherNodes == null || otherNodes.isEmpty())
					exclusiveGroups.put(projection, nodes);
				else {
					// Merge source and target nodes
					BitSet merged = (BitSet)nodes.clone();
					merged.or(otherNodes);
					exclusiveGroups.put(projection, merged);
				}
			}
		}
		return exclusiveGroups;
	}
	
	private void addNonSharedGroups(
		Map<Map<Attribute, String>, BitSet> sharedGroups,
		Map<Map<Attribute, String>, BitSet> newGroups
	) {
		for (Entry<Map<Attribute, String>, BitSet> g : newGroups.entrySet()) {
			Map<Attribute, String> projection = g.getKey();
			BitSet nodes = g.getValue();
			BitSet otherNodes = sharedGroups.get(projection);
			if (nodes != null && !nodes.isEmpty() && (otherNodes == null || otherNodes.isEmpty()))
				sharedGroups.put(projection, nodes);
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
	private static Stream<AttributeHierarchyNode> units(Task t, Location l) {
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
	private Map<Map<Attribute, String>, BitSet> groupNodesBy(Input input, List<List<TaskUnitNode>> locations, Scope scope) {
		return locations == null ? null 
			: locations.stream()
				.flatMap(List::stream)
				.collect(Collectors.groupingBy(
					n -> scope.project(n.getUnit()),
					Collector.of(
						() -> new BitSet(input.nodeCount()),
						(bitSet, node) -> bitSet.set(input.indexOf(node)),
						(bs1, bs2) -> { bs1.or(bs2); return bs1; },
						Characteristics.IDENTITY_FINISH
					) 
				));
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
