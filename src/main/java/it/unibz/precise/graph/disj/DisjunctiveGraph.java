package it.unibz.precise.graph.disj;

import it.unibz.precise.graph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
public class DisjunctiveGraph<T> implements Cloneable, Graph<T> {
	
	private Set<T> nodes;
	private Set<Arc<T>> arcs;
	private Set<DisjunctiveEdge<T>> edges;

	private Map<T, Set<T>> succ = new HashMap<>();		// Map from node to successors
	private Map<T, Set<T>> pred = new HashMap<>();		// Map from node to predecessors
	
	// disjunctive edges
	private Map<T, Set<DisjunctiveEdge<T>>> disj = new HashMap<>();		// Map from nodes to disjunctive edges that contains them
	
	public DisjunctiveGraph() {
		this.nodes = new HashSet<>();
		this.arcs = new HashSet<>();
		this.edges = new HashSet<>();
	}
	
	public DisjunctiveGraph(DisjunctiveGraph<T> other) {
		this(other.nodes, other.arcs, other.edges);
	}
	
	public DisjunctiveGraph(Collection<T> nodes, Collection<Arc<T>> arcs, Collection<DisjunctiveEdge<T>> edges) {
		this.nodes = new HashSet<>(nodes);
		this.arcs = new HashSet<>(arcs.size());
		this.edges = new HashSet<>(edges.size());
		addAllArcs(arcs);
		addAllEdges(edges);
	}
	
	public Set<T> nodes() {
		return nodes;
	}
	
	public Stream<T> successors(T n) {
		return successorSet(n).stream();
	}
	
	public Set<T> successorSet(T n) {
		return checkSet(succ.get(n));
	}
	
	public Set<T> predecessorSet(T n) {
		return checkSet(pred.get(n));
	}
	
	private <E> Set<E> checkSet(Set<E> set) {
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
		// Only add arc if there is no arc with same source and target
		return addToMultimap(succ, arc.getSource(), arc.getTarget())
			&& addToMultimap(pred, arc.getTarget(), arc.getSource())
			&& arcs.add(arc);
	}
	
	public boolean addAllEdges(Collection<DisjunctiveEdge<T>> edges) {
		boolean added = false;
		for (DisjunctiveEdge<T> e : edges)
			added |= addEdge(e);
		return added;
	}
	
	public boolean addEdge(DisjunctiveEdge<T> edge) {
		// Only add edge if it is not already contained
		return addAllToMultimap(disj, edge.getLeft(), edge)
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
		return new DisjunctiveGraph<T>(nodes, arcs, edges);
	}
	
	/** Prints the given graph for debugging. */
	public void print() {
		System.out.println("ARCS:");
		printLinesIndented(arcs);
		System.out.println("EDGES:");
		printLinesIndented(edges);
	}
	
	/** Helper method for printing all the given lines with one tab for indentation, or (none) if elements is empty. */
	private <E> void printLinesIndented(Collection<E> elements) {
		if (elements.isEmpty())
			System.out.println("\t(none)");
		else {
			elements.stream()
			.map(a -> "\t" + a)
			.forEach(System.out::println);
		}
	}

}
