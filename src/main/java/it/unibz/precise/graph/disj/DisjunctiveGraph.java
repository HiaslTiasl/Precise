package it.unibz.precise.graph.disj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.precise.graph.MaterializedGraph;

/**
 * A disjunctive graph representation.
 * Has a set of nodes, conjunctive arcs, and disjunctive edges.
 * Supports orienting edges into a particular direction, effectively
 * removing the edge and adding arcs from all nodes of the one side
 * to all nodes of the other side.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes.
 */
public class DisjunctiveGraph<T> implements Cloneable, MaterializedGraph<T> {
	
	private Set<T> nodes;
	private Set<Arc<T>> arcs;
	private Set<DisjunctiveEdge<T>> edges;

	private Map<T, Set<T>> succ = new HashMap<>();		// Map from node to successors
	private Map<T, Set<T>> pred = new HashMap<>();		// Map from node to predecessors
	
	// N.B. this auxiliary data structure is used to speed up the algorithm.
	// For example, it allows to resolve disjunctive edges found on the way while trying to resolve another disjunctive edge.
	// However, if many nodes are involved in many disjunctive edges, it requires a lot of memory.
	private Map<T, Set<DisjunctiveEdge<T>>> disj = new HashMap<>();		// Map from nodes to disjunctive edges that contains them
	
	public DisjunctiveGraph() {
		this(new HashSet<>());
	}
	
	/** Creates a new graph with the given nodes only. */
	private DisjunctiveGraph(Set<T> nodes) {
		this.nodes = nodes;
		this.arcs = new HashSet<>();
		this.edges = new HashSet<>();
	}
	
	/**
	 * Creates a graph with the given nodes and adds all given arcs and edges.
	 * The set of nodes is used as is, whereas arcs and edges are copied into new collections.
	 */
	private DisjunctiveGraph(Set<T> nodes, Collection<Arc<T>> arcs, Collection<DisjunctiveEdge<T>> edges) {
		this.nodes = nodes;
		this.arcs = new HashSet<>(arcs.size());
		this.edges = new HashSet<>(edges.size());
		addAllArcs(arcs);
		addAllEdges(edges);
	}
	
	/** Copy the given graph. */
	public static <T> DisjunctiveGraph<T> copy(DisjunctiveGraph<T> other) {
		return new DisjunctiveGraph<>(new HashSet<>(other.nodes), other.arcs, other.edges);
	}
	
	/**
	 * Copy the given graph with sealed nodes.
	 * This prevents to add further nodes afterwards and avoids copying nodes.
	 */
	public static <T> DisjunctiveGraph<T> copySealedNodes(DisjunctiveGraph<T> other) {
		return new DisjunctiveGraph<>(Collections.unmodifiableSet(other.nodes), other.arcs, other.edges);
	}
	
	/**
	 * Create a graph with the given sealed nodes.
	 * This prevents to add further nodes afterwards and avoids copying nodes.
	 */
	public static <T> DisjunctiveGraph<T> sealedNodes(Set<T> nodes) {
		return new DisjunctiveGraph<>(Collections.unmodifiableSet(nodes));
	}
	
	/**
	 * Creates a subgraph of this graph consisting only of the given nodes.
	 * This implementation assumes that the given nodes are all contained in this graph.
	 * If this is not the case, the behavior is unspecified.
	 */
	public DisjunctiveGraph<T> restrictedTo(Set<T> nodes) {
		return new DisjunctiveGraph<>(Collections.unmodifiableSet(nodes), arcs, edges);
	}
	
	public Set<T> nodes() {
		return nodes;
	}
	
	public Set<T> successorSet(T n) {
		return checkSet(succ.get(n));
	}
	
	public Set<T> predecessorSet(T n) {
		return checkSet(pred.get(n));
	}
	
	private static <E> Set<E> checkSet(Set<E> set) {
		return set != null ? set : Collections.emptySet();
	}
	
	public Set<Arc<T>> arcs() {
		return arcs;
	}
	
	public Set<DisjunctiveEdge<T>> edges() {
		return edges;
	}
	
	public Set<DisjunctiveEdge<T>> disjunctions(T n) {
		return checkSet(disj.get(n));
	}
	
	public boolean addAllNodes(Collection<T> nodes) {
		return this.nodes.addAll(nodes);
	}
	
	public boolean addNode(T n) {
		return nodes.add(n);
	}
	
	public boolean addAllArcs(Collection<Arc<T>> arcs) {
		boolean added = false;
		for (Arc<T> a : arcs)
			added |= addArc(a);
		return added;
	}
	
	public boolean addAllArcs(Collection<T> sources, Collection<T> target) {
		boolean added = false;
		for (T s : sources) {
			for (T t : target)
				added |= addArc(new Arc<>(s, t));
		}
		return added;
	}

	public boolean addArc(Arc<T> arc) {
		T source = arc.getSource(), target = arc.getTarget();
		// N.B: arcs is a set, and can only contain an arc once.
		// Also, only add arc if both source and target are already nodes in this graph
		return nodes.contains(source) && nodes.contains(target)
			&& arcs.add(arc)
			&& addToMultimap(succ, arc.getSource(), arc.getTarget())
			&& addToMultimap(pred, arc.getTarget(), arc.getSource());
	}
	
	public boolean removeArc(Arc<T> arc) {
		return arcs.remove(arc)
			&& removeFrom(succ, arc.getSource(), arc.getTarget())
			&& removeFrom(pred, arc.getTarget(), arc.getSource());
	}
	
	public boolean addAllEdges(Collection<DisjunctiveEdge<T>> edges) {
		boolean added = false;
		for (DisjunctiveEdge<T> e : edges)
			added |= addEdge(e);
		return added;
	}
	
	public boolean addEdge(DisjunctiveEdge<T> edge) {
		Set<T> left = edge.getLeft(), right = edge.getRight();
		// N.B: edges is a set, and can only contain an arc once.
		// Also, only add edge if all connected nodes are already nodes in this graph
		return nodes.containsAll(left) && nodes.containsAll(right)
			&& addAllToMultimap(disj, edge.getLeft(), edge)
			&& addAllToMultimap(disj, edge.getRight(), edge)
			&& edges.add(edge);
	}
	
	public boolean removeEdge(DisjunctiveEdge<T> edge) {
		return edges.remove(edge)
			&& removeFromAll(disj, edge.getLeft(), edge)
			&& removeFromAll(disj, edge.getRight(), edge);
	}
	
	/**
	 * Orients all disjunctive edges from source to target.
	 * If a disjunctive edge contains {@code source} on the one side
	 * and {@code target} on the other side, the edge is removed and
	 * arcs from all nodes of the side containing {@code source} to
	 * all nodes of the side containing {@code target} are added.
	 * @return true if any edges were removed, false otherwise.
	 */
	public boolean orient(T source, T target) {
		boolean oriented = false;
		// Choose smaller set to avoid iterations
		Set<DisjunctiveEdge<T>> sourceDisj = disjunctions(source);
		Set<DisjunctiveEdge<T>> targetDisj = disjunctions(target);
		Set<DisjunctiveEdge<T>> disj = sourceDisj.size() < targetDisj.size() ? sourceDisj : targetDisj;
		
		// Add edges to lists, then remove, to avoid ConcurrentModificationException
		ArrayList<DisjunctiveEdge<T>> leftToRight = new ArrayList<>();
		ArrayList<DisjunctiveEdge<T>> rightToLeft = new ArrayList<>();
		
		for (DisjunctiveEdge<T> e : disj) {
			if (e.excludes(source, target))
				leftToRight.add(e);
			else if (e.excludes(target, source))
				rightToLeft.add(e);
		}
		for (DisjunctiveEdge<T> e : leftToRight)
			oriented |= orient(e, e.getLeft(), e.getRight());
		for (DisjunctiveEdge<T> e : rightToLeft)
			oriented |= orient(e, e.getRight(), e.getLeft());
			
		return oriented;
	}
	
	/** Orients the given disjunctive edge in the given direction. */
	public boolean orient(DisjunctiveEdge<T> e, Set<T> from, Set<T> to) {
		return removeEdge(e)
			&& addAllArcs(from, to);
	}
	
	/** Adds {@code value} to the set of values stored in {@code map} under {@code key}. */
	private <K, V> boolean addToMultimap(Map<K, Set<V>> map, K key, V value) {
		return map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
	}
	
	/** Adds {@code value} to all sets of values stored in {@code map} under {@code keys}. */
	private <K, V> boolean addAllToMultimap(Map<K, Set<V>> map, Set<K> keys, V value) {
		boolean added = false;
		for (K key : keys)
			added |= addToMultimap(map, key, value);
		return added;
	}
	
	
	/** Remove {@code value} from the set of values stored in {@code map} under {@code key}. */
	private <K, V> boolean removeFrom(Map<K, Set<V>> map, K key, V value) {
		Set<V> values = map.get(key);
		return values != null && values.remove(value);
	}

	/** Remove {@code value} from all sets of values stored in {@code map} under {@code keys}. */
	private <K, V> boolean removeFromAll(Map<K, Set<V>> map, Set<K> keys, V value) {
		boolean removed = false;
		for (K k : keys)
			removed |= map.get(k).remove(value);
		return removed;
	}
	
	@Override
	public Object clone() {
		// N.B: Does not clone nodes, arcs, or edges!
		return copy(this);
	}

}
