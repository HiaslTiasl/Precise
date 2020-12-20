package it.unibz.precise.graph.disj;

import it.unibz.precise.graph.Graph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/**
 * A disjunctive graph representation.
 * Has a set of nodes, conjunctive arcs, and disjunctive edges.
 * Supports orienting edges into a particular direction, effectively
 * removing the edge and adding arcs from all nodes of the one side
 * to all nodes of the other side.
 *
 * @author MatthiasP
 */
public class DisjunctiveGraph implements Cloneable, Graph.Eager {

	private int arcCount;

	private int nodes;
	private BitSet[] adj;
	private Set<DisjunctiveEdge> edges;

	private IntUnaryOperator toOriginalNode;

	// N.B. The following auxiliary data structure was used to speed up the algorithm.
	// In particular, it allows to resolve disjunctive edges found on the way while trying to resolve another disjunctive edge (see ResolveType.SOME).
	// However, if many nodes are involved in many disjunctive edges, it requires a lot of memory.
	// Also, it looks as if that in turn this also requires time to fill the structure.
	// Therefore, it was dropped
	//private Map<T, Set<DisjunctiveEdge<T>>> disj = new HashMap<>();		// Map from nodes to disjunctive edges that contains them

	/** Creates a new graph with the given nodes only. */
	private DisjunctiveGraph(int nodes, IntUnaryOperator toOriginalNode) {
		this.nodes = nodes;
		this.adj = new BitSet[nodes];
		this.edges = new HashSet<>();
		this.toOriginalNode = toOriginalNode;
	}

	/**
	 * Creates a graph with the given nodes and adds all given arcs and edges.
	 * The set of nodes is used as is, whereas arcs and edges are copied into new collections.
	 */
	private DisjunctiveGraph(BitSet[] adj, HashSet<DisjunctiveEdge> edges, IntUnaryOperator toOriginalNode) {
		this.nodes = adj.length;
		this.adj = adj;
		this.edges = edges;
		this.toOriginalNode = toOriginalNode;
	}

	/** Copy the given graph. */
	public static DisjunctiveGraph copy(DisjunctiveGraph other) {
		return new DisjunctiveGraph(
			Arrays.copyOf(other.adj, other.adj.length),
			new HashSet<>(other.edges),
			other.toOriginalNode
		);
	}

	/**
	 * Copy the given graph with sealed nodes.
	 * This prevents to add further nodes afterwards and avoids copying nodes.
	 */
	public static DisjunctiveGraph copySealedNodes(DisjunctiveGraph other) {
		return new DisjunctiveGraph(
			Arrays.copyOf(other.adj, other.adj.length),
			new HashSet<>(other.edges),
			other.toOriginalNode
		);
	}

	/**
	 * Create a graph with the given sealed nodes.
	 * This prevents to add further nodes afterwards and avoids copying nodes.
	 */
	public static DisjunctiveGraph sealedNodes(int nodes) {
		return new DisjunctiveGraph(nodes, null);
	}

	/**
	 * Creates a subgraph of this graph consisting only of the given nodes.
	 * This implementation assumes that the given nodes are all contained in this graph.
	 * If this is not the case, the behavior is unspecified.
	 */
	public DisjunctiveGraph restrictedTo(BitSet subset) {
		int nodes = subset.cardinality();
		BitSet[] newAdj = new BitSet[nodes];
		HashSet<DisjunctiveEdge> edges = new HashSet<>(this.edges.size());
		int[] mapping = new int[nodes];
		int curNode = -1;
		for (int i = 0; i < nodes; i++) {
			curNode = subset.nextSetBit(curNode + 1);
			newAdj[i] = pick(this.adj[curNode], nodes, subset);
			mapping[i] = curNode;
		}
		for (DisjunctiveEdge oldEdge : this.edges) {
			BitSet newLeft = pick(oldEdge.getLeft(), nodes, subset);
			if (newLeft.isEmpty())
				continue;
			BitSet newRight = pick(oldEdge.getRight(), nodes, subset);
			if (newRight.isEmpty())
				continue;
			edges.add(new DisjunctiveEdge(newLeft, newRight));
		}
		return new DisjunctiveGraph(newAdj, edges, node -> toOriginalNode(mapping[node]));
	}

	private static BitSet pick(BitSet org, int size, BitSet mask) {
		BitSet restricted = new BitSet(size);
		for (int i = 0, bit = -1; i < size; i++) {
			bit = mask.nextSetBit(bit + 1);
			restricted.set(i,  mask.get(bit));
		}
		return restricted;
	}

	public int nodes() {
		return nodes;
	}

	public int toOriginalNode(int node) {
		return toOriginalNode != null ? toOriginalNode.applyAsInt(node) : node;
	}

	public BitSet allSuccessors(int n) {
		return adj[n];
	}

	private static <E> Set<E> checkSet(Set<E> set) {
		return set != null ? set : Collections.emptySet();
	}

	public int arcCount() {
		return arcCount;
	}

	public BitSet[] arcs() {
		return adj;
	}

	public Set<DisjunctiveEdge> edges() {
		return edges;
	}

//	public Set<DisjunctiveEdge<T>> disjunctions(T n) {
//		return checkSet(disj.get(n));
//	}

	public boolean addAllArcs(BitSet[] adj) {
		boolean added = false;
		for (int n = 0; n < nodes; n++) {
			BitSet oldSuccs = this.adj[n];
			BitSet newSuccs = adj[n];
			int oldSuccsSize = oldSuccs.size();
			int newSuccsSize = newSuccs.size();
			if (newSuccsSize <= oldSuccsSize)
				oldSuccs.or(newSuccs);
			else {
				for (int s = newSuccs.nextSetBit(0); s >= 0 && s < nodes; s = newSuccs.nextSetBit(s + 1))
					oldSuccs.set(s);
			}
			added |= oldSuccsSize != oldSuccs.size();
		}
		return added;
	}

	public void addSuccessors(int node, BitSet successors) {
		adj[node].or(successors);
	}

	public void addAllArcs(BitSet sources, BitSet targets) {
		int src = -1;
		while ((src = sources.nextSetBit(src + 1)) >= 0) {
			addSuccessors(src, targets);
		}
	}

	public boolean addEdge(BitSet left, BitSet right) {
		return addEdge(new DisjunctiveEdge(left, right));
	}

	public boolean addEdge(DisjunctiveEdge edge) {
		BitSet left = edge.getLeft(), right = edge.getRight();
		if (left.size() != nodes || right.size() != nodes)
			throw new IllegalArgumentException("Groups of DisjunctiveEdge do not match Graph size");
		// N.B: edges is a set, and can only contain an arc once.
		// Also, only add edge if all connected nodes are already nodes in this graph
		return
			//&& addToMultimapUnderAll(disj, edge.getLeft(), edge)
			//&& addToMultimapUnderAll(disj, edge.getRight(), edge)
			edges.add(edge);
	}

	public boolean removeEdge(DisjunctiveEdge edge) {
		return edges.remove(edge);
			//&& removeFromAll(disj, edge.getLeft(), edge)
			//&& removeFromAll(disj, edge.getRight(), edge);
	}

	/** Orients the given disjunctive edge in the given direction. */
	public void orient(DisjunctiveEdge e, BitSet from, BitSet to) {
		if (removeEdge(e))
			addAllArcs(from, to);
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
