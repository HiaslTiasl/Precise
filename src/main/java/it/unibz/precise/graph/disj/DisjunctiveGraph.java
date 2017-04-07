package it.unibz.precise.graph.disj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	private int arcCount;
	
	private Set<T> nodes;
	private Set<DisjunctiveEdge<T>> edges;

	private Map<T, Set<T>> succ = new HashMap<>();		// Map from node to successors
	
	// N.B. The following auxiliary data structure was used to speed up the algorithm.
	// In particular, it allows to resolve disjunctive edges found on the way while trying to resolve another disjunctive edge (see ResolveType.SOME).
	// However, if many nodes are involved in many disjunctive edges, it requires a lot of memory.
	// Also, it looks as if that in turn this also requires time to fill the structure.
	// Therefore, it was dropped
	//private Map<T, Set<DisjunctiveEdge<T>>> disj = new HashMap<>();		// Map from nodes to disjunctive edges that contains them
	
	public DisjunctiveGraph() {
		this(new HashSet<>());
	}
	
	/** Creates a new graph with the given nodes only. */
	private DisjunctiveGraph(Set<T> nodes) {
		this.nodes = nodes;
		this.edges = new HashSet<>();
	}
	
	/**
	 * Creates a graph with the given nodes and adds all given arcs and edges.
	 * The set of nodes is used as is, whereas arcs and edges are copied into new collections.
	 */
	private DisjunctiveGraph(Set<T> nodes, Map<T, Set<T>> succ, Collection<DisjunctiveEdge<T>> edges) {
		this.nodes = nodes;
		this.edges = new HashSet<>(edges.size());
		addAllArcs(succ);
		addAllEdges(edges);
	}
	
	/** Copy the given graph. */
	public static <T> DisjunctiveGraph<T> copy(DisjunctiveGraph<T> other) {
		return new DisjunctiveGraph<>(new HashSet<>(other.nodes), other.succ, other.edges);
	}
	
	/**
	 * Copy the given graph with sealed nodes.
	 * This prevents to add further nodes afterwards and avoids copying nodes.
	 */
	public static <T> DisjunctiveGraph<T> copySealedNodes(DisjunctiveGraph<T> other) {
		return new DisjunctiveGraph<>(Collections.unmodifiableSet(other.nodes), other.succ, other.edges);
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
		return new DisjunctiveGraph<>(Collections.unmodifiableSet(nodes), succ, edges);
	}
	
	public Set<T> nodes() {
		return nodes;
	}
	
	public Set<T> successorSet(T n) {
		return checkSet(succ.get(n));
	}
	
	private static <E> Set<E> checkSet(Set<E> set) {
		return set != null ? set : Collections.emptySet();
	}
	
	public int arcCount() {
		return arcCount;
	}
	
	public Map<T, Set<T>> arcs() {
		return succ;
	}
	
	public Set<DisjunctiveEdge<T>> edges() {
		return edges;
	}

//	public Set<DisjunctiveEdge<T>> disjunctions(T n) {
//		return checkSet(disj.get(n));
//	}
	
	public boolean addAllNodes(Collection<T> nodes) {
		return this.nodes.addAll(nodes);
	}
	
	public boolean addNode(T n) {
		return nodes.add(n);
	}
	
	public boolean addAllArcs(Map<T, Set<T>> succ) {
		boolean added = false;
		for (Map.Entry<T, Set<T>> entry : succ.entrySet()) {
			T source = entry.getKey();
			for (T target : entry.getValue())
				added |= addArc(source, target);
		}
		return added;
	}
	
	public boolean addAllArcs(Collection<T> sources, Collection<T> targets) {
		boolean added = false;
		for (T s : sources) {
			for (T t : targets)
				added |= addArc(s, t);
		}
		return added;
	}
	
	public boolean addArc(T source, T target) {
		// N.B: arcs is a set, and can only contain an arc once.
		// Also, only add arc if both source and target are already nodes in this graph
		boolean added = nodes.contains(source) && nodes.contains(target)
			&& addToMultimap(succ, source, target);
		if (added)
			arcCount++;
		return added;
	}
	
	public boolean removeArc(T source, T target) {
		boolean removed = removeFrom(succ, source, target);
		if (removed)
			arcCount--;
		return removed;
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
			//&& addToMultimapUnderAll(disj, edge.getLeft(), edge)
			//&& addToMultimapUnderAll(disj, edge.getRight(), edge)
			&& edges.add(edge);
	}
	
	public boolean removeEdge(DisjunctiveEdge<T> edge) {
		return edges.remove(edge);
			//&& removeFromAll(disj, edge.getLeft(), edge)
			//&& removeFromAll(disj, edge.getRight(), edge);
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
	
	/** Remove {@code value} from the set of values stored in {@code map} under {@code key}. */
	private <K, V> boolean removeFrom(Map<K, Set<V>> map, K key, V value) {
		Set<V> values = map.get(key);
		return values != null && values.remove(value);
	}

	@Override
	public Object clone() {
		// N.B: Does not clone nodes, arcs, or edges!
		return copy(this);
	}

//	/**
//	 * Orients all disjunctive edges from source to target.
//	 * If a disjunctive edge contains {@code source} on the one side
//	 * and {@code target} on the other side, the edge is removed and
//	 * arcs from all nodes of the side containing {@code source} to
//	 * all nodes of the side containing {@code target} are added.
//	 * @return true if any edges were removed, false otherwise.
//	 */
//	public boolean orient(T source, T target) {
//		boolean oriented = false;
//		// Choose smaller set to avoid iterations
//		Set<DisjunctiveEdge<T>> sourceDisj = disjunctions(source);
//		Set<DisjunctiveEdge<T>> targetDisj = disjunctions(target);
//		Set<DisjunctiveEdge<T>> disj = sourceDisj.size() < targetDisj.size() ? sourceDisj : targetDisj;
//		
//		// Add edges to lists, then remove, to avoid ConcurrentModificationException
//		ArrayList<DisjunctiveEdge<T>> leftToRight = new ArrayList<>();
//		ArrayList<DisjunctiveEdge<T>> rightToLeft = new ArrayList<>();
//		
//		for (DisjunctiveEdge<T> e : disj) {
//			if (e.excludes(source, target))
//				leftToRight.add(e);
//			else if (e.excludes(target, source))
//				rightToLeft.add(e);
//		}
//		for (DisjunctiveEdge<T> e : leftToRight)
//			oriented |= orient(e, e.getLeft(), e.getRight());
//		for (DisjunctiveEdge<T> e : rightToLeft)
//			oriented |= orient(e, e.getRight(), e.getLeft());
//			
//		return oriented;
//	}

}
