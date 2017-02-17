package it.unibz.precise.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;

/**
 * Represents the result of searching an acyclic orientation, which can be successful or not.
 * A {@code OrientationResult} is structured in a tree corresponding to the recursive graph
 * partitions performed during the search.
 * An {@code OrientationResult} that is not a leaf {@link #isSuccessful() is successful} iff
 * all contained leaves are successful.
 * Therefore, a successful {@code OrientationResult} in general does not actually hold an
 * acyclic orientation of the input graph, but rather just contains the information needed
 * to construct one.
 * 
 * @author MatthiasP
 * 
 * @param <T> The type of the nodes in the graph.
 */
public abstract class OrientationResult<T> {
	
	private DisjunctiveGraph<T> graph;
	private boolean successful;
	
	private OrientationResult(DisjunctiveGraph<T> graph, boolean success) {
		this.graph = graph;
		this.successful = success;
	}

	/** Returns the (possibly simplified) input graph. */
	public DisjunctiveGraph<T> getGraph() {
		return graph;
	}
	
	/** Indicates if the input graph has an acyclic orientation. */
	public boolean isSuccessful() {
		return successful;
	}

	/** Returns the immediate children */
	public abstract List<OrientationResult<T>> children();
	
	/** Returns a stream of all nodes in the result tree. */
	public abstract Stream<OrientationResult<T>> flatten();
	
	/** Returns a stream of all leaf nodes in the result tree. */
	public Stream<OrientationResult.Leaf<T>> leaves() {
		return leafs(false);
	}
	
	/** Returns a stream of all failure leaf nodes in the result tree. */ 
	public Stream<OrientationResult.Leaf<T>> failureReasons() {
		return leafs(true);
	}
	
	/**
	 * Returns a stream of all leaf nodes in the result tree.
	 * If {@code onlyFailures} is set to {@literal true}, successful results are
	 * omitted.
	 */
	protected abstract Stream<OrientationResult.Leaf<T>> leafs(boolean onlyFailures);
	
	/**
	 * Project a lists of {@link OrientationResult}s for a list of clusters to a graph
	 * of results, using the
	 */
	public DisjunctiveGraph<T> buildOrientation() {
		// Can only build orientations for successful results
		if (!successful)
			return null;
		
		List<OrientationResult.Leaf<T>> leaves = leaves().collect(Collectors.toList());
		int count = leaves.size();
		
		DisjunctiveGraph<T> orientation = DisjunctiveGraph.sealedNodes(graph.nodes());
		orientation.addAllArcs(graph.arcs());
		
		// Map each node in to the cluster in which it is contained
		Map<T, Integer> leafClusterIndexMap = new HashMap<>();
		for (int i = 0; i < count; i++) {
			DisjunctiveGraph<T> g = leaves.get(i).getGraph();
			orientation.addAllArcs(g.arcs());
			for (T n : leaves.get(i).getGraph().nodes())
				leafClusterIndexMap.put(n, i);
		}
		
		for (DisjunctiveEdge<T> e : graph.edges()) {
			Set<T> left = e.getLeft(), right = e.getRight();
			// It is guaranteed that the two sides of an edge are each contained in a cluster
			// at the same level in the result tree, respectively.
			// For a level higher than leaves, the nodes might be contained in different leaf
			// graphs. However, this is not a problem, because the topological order still
			// applies.
			int iLeft = anyValue(leafClusterIndexMap, left);
			int iRight = anyValue(leafClusterIndexMap, right);
			if (iLeft < iRight)
				orientation.addAllArcs(left, right);
			else if (iRight < iLeft)
				orientation.addAllArcs(right, left);
			//else {
				// The edge is contained in the graph of a leaf result.
				// But then it already has been resolved to arcs, we already added all arcs above.
				// So nothing to do here in that case
			//}
		}
		
		return orientation;
	}
	
	private static <K,V> V anyValue(Map<K, V> map, Set<K> keys) {
		return map.get(keys.stream().findAny().orElse(null));
	}
	
	/**
	 * Represents a complex {@code OrientationResult} composed of several {@link #children() children} results.
	 * A complex result is successful if all child results are successful.
	 * 
	 * @author MatthiasP
	 *
	 * @param <T> The type of the nodes in the graph.
	 */
	public static final class Complex<T> extends OrientationResult<T> {
		
		private List<OrientationResult<T>> children;
		
		private Complex(DisjunctiveGraph<T> resolved, List<OrientationResult<T>> children) {
			super(resolved, children.stream().allMatch(OrientationResult::isSuccessful));
			this.children = children;
		}
		
		@Override
		public List<OrientationResult<T>> children() {
			return children;
		}
		
		@Override
		protected Stream<OrientationResult.Leaf<T>> leafs(boolean onlyFailures) {
			return onlyFailures && isSuccessful()	// A successful result does not contain any failure leaves.
				? Stream.empty()
				: children.stream().flatMap(r -> r.leafs(onlyFailures));
		}
		
		@Override
		public Stream<OrientationResult<T>> flatten() {
			return Stream.concat(
				Stream.of(this),
				children.stream().flatMap(OrientationResult::flatten)
			);
		}
		
	}

	/**
	 * Represents a leaf result.
	 * If successful, the given graph is already an acyclic orientation of itself.
	 * Otherwise, either there are cycles or there is a deadlock.
	 * A cycle is indicated by a list of non-empty strongly connected components ({@link #getSccs() SCCs}).
	 * A deadlock is indicated by an edge that cannot be oriented in any direction without introducing
	 * another problem.
	 * 
	 * @author MatthiasP
	 *
	 * @param <T> The type of the nodes in the graph.
	 */
	public static final class Leaf<T> extends OrientationResult<T> {
		
		private List<List<T>> sccs;
		private DisjunctiveEdge<T> deadlockEdge;
		
		private Leaf(DisjunctiveGraph<T> resolved, boolean success, List<List<T>> sccs, DisjunctiveEdge<T> deadlockEdge) {
			super(resolved, success);
			this.sccs = sccs;
			this.deadlockEdge = deadlockEdge;
		}
		
		@Override
		public List<OrientationResult<T>> children() {
			return Collections.emptyList();
		}
		
		@Override
		public Stream<OrientationResult.Leaf<T>> leafs(boolean onlyFailures) {
			return onlyFailures && isSuccessful()
				? Stream.empty()
				: Stream.of(this);
		}
		
		@Override
		public Stream<OrientationResult<T>> flatten() {
			return Stream.of(this);
		}
		
		/** Returns the Strongly-Connected Components in the given graph. */
		public List<List<T>> getSccs() {
			return sccs;
		}
		
		/** Returns the edge that leads to a deadlock in the given graph. */
		public DisjunctiveEdge<T> getDeadlockEdge() {
			return deadlockEdge;
		}
	}
	
	/** Creates a result composed of the given clusters and children results. */
	public static <T> OrientationResult<T> compose(DisjunctiveGraph<T> graph, List<OrientationResult<T>> children) {
		return children.size() <= 1 
			? children.stream().findAny().orElseGet(() -> success(graph))
			: new Complex<>(graph, children);
	}

	/** Creates a successful result. */
	public static <T> OrientationResult.Leaf<T> success(DisjunctiveGraph<T> graph) {
		return new Leaf<>(graph, true, null, null);
	}
	
	/** Creates a failure because of the given strongly connected components. */
	public static <T> OrientationResult.Leaf<T> error(DisjunctiveGraph<T> graph, List<List<T>> sccs) {
		return new Leaf<>(graph, false, sccs, null);
	}
	
	/** Creates a failure because of a deadlock represented by the given edge. */
	public static <T> OrientationResult.Leaf<T> error(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> deadlockEdge) {
		return new Leaf<>(graph, false, null, deadlockEdge);
	}
	
}