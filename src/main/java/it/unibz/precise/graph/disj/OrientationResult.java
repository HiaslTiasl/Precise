package it.unibz.precise.graph.disj;

import java.util.*;
import java.util.stream.Stream;

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
 */
public abstract class OrientationResult {
	
	private DisjunctiveGraph graph;
	private boolean successful;
	
	private OrientationResult(DisjunctiveGraph graph, boolean success) {
		this.graph = graph;
		this.successful = success;
	}

	/** Returns the (possibly simplified) input graph. */
	public DisjunctiveGraph getGraph() {
		return graph;
	}
	
	/** Indicates if the input graph has an acyclic orientation. */
	public boolean isSuccessful() {
		return successful;
	}

	/** Returns the immediate children in topological order. */
	public abstract List<OrientationResult> children();
	
	/** Returns a stream of all nodes in the result tree. */
	public abstract Stream<OrientationResult> flatten();
	
	/** Returns a stream of all leaf nodes in the result tree. */
	public Stream<OrientationResult.Leaf> leaves() {
		return leafs(false);
	}
	
	/** Returns a stream of all failure leaf nodes in the result tree. */ 
	public Stream<OrientationResult.Leaf> failureReasons() {
		return leafs(true);
	}
	
	/**
	 * Returns a stream of all leaf nodes in the result tree.
	 * If {@code onlyFailures} is set to {@literal true}, successful results are
	 * omitted.
	 */
	protected abstract Stream<OrientationResult.Leaf> leafs(boolean onlyFailures);
	
	/**
	 * Project a lists of {@link OrientationResult}s for a list of clusters to a graph
	 * of results, using the
	 */
	public DisjunctiveGraph buildOrientation() {
		// Can only build orientations for successful results
		if (!successful)
			return null;

		DisjunctiveGraph orientation = DisjunctiveGraph.copy(graph);
		
		// Map each node in to the cluster in which it is contained
		// TODO use integers in the original graph, if possible
		int[] leafClusterIndexMap = new int[graph.nodes()];
		int clusterIndex = 0;
		for (Iterator<Leaf> leaves = leaves().iterator(); leaves.hasNext(); clusterIndex++) {
			OrientationResult leaf = leaves.next();
			int leafGraphNodes = leaf.graph.nodes();
			for (int n = 0; n < leafGraphNodes; n++)
				leafClusterIndexMap[leaf.graph.toOriginalNode(n)] = clusterIndex;
		}
		
		for (DisjunctiveEdge e : graph.edges()) {
			BitSet left = e.getLeft(), right = e.getRight();
			// It is guaranteed that the two sides of an edge are each contained in a cluster
			// at the same level in the result tree, respectively.
			// For a level higher than leaves, the nodes of one side might be contained in
			// different leaf graphs. However, this is not a problem, because the topological
			// order still applies.
			int anyLeft = left.nextSetBit(0);
			int anyRight = right.nextSetBit(0);
			int iLeft = leafClusterIndexMap[anyLeft];
			int iRight = leafClusterIndexMap[anyRight];
			boolean l2r = iLeft < iRight
				|| iLeft == iRight && graph.allSuccessors(anyLeft).get(anyRight);
			if (l2r)
				orientation.addAllArcs(left, right);
			else if (iRight < iLeft)
				orientation.addAllArcs(right, left);
		}
		
		return orientation;
	}
	
	/**
	 * Represents a complex {@code OrientationResult} composed of several {@link #children() children} results.
	 * A complex result is successful if all child results are successful.
	 * 
	 * @author MatthiasP
	 */
	public static final class Complex extends OrientationResult {
		
		private List<OrientationResult> children;	// The children in topological order
		
		private Complex(DisjunctiveGraph resolved, List<OrientationResult> children) {
			super(resolved, children.stream().allMatch(OrientationResult::isSuccessful));
			this.children = children;
		}
		
		@Override
		public List<OrientationResult> children() {
			return children;
		}
		
		@Override
		protected Stream<OrientationResult.Leaf> leafs(boolean onlyFailures) {
			return onlyFailures && isSuccessful()	// A successful result does not contain any failure leaves.
				? Stream.empty()
				: children.stream().flatMap(r -> r.leafs(onlyFailures));
		}
		
		@Override
		public Stream<OrientationResult> flatten() {
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
	 */
	public static final class Leaf extends OrientationResult {
		
		private List<BitSet> sccs;
		private DisjunctiveEdge deadlockEdge;
		
		private Leaf(DisjunctiveGraph resolved, boolean success, List<BitSet> sccs, DisjunctiveEdge deadlockEdge) {
			super(resolved, success);
			this.sccs = sccs;
			this.deadlockEdge = deadlockEdge;
		}
		
		@Override
		public List<OrientationResult> children() {
			return Collections.emptyList();
		}
		
		@Override
		public Stream<OrientationResult.Leaf> leafs(boolean onlyFailures) {
			return onlyFailures && isSuccessful()
				? Stream.empty()
				: Stream.of(this);
		}
		
		@Override
		public Stream<OrientationResult> flatten() {
			return Stream.of(this);
		}
		
		/** Returns the Strongly-Connected Components in the given graph. */
		public List<BitSet> getSccs() {
			return sccs;
		}
		
		/** Returns the edge that leads to a deadlock in the given graph. */
		public DisjunctiveEdge getDeadlockEdge() {
			return deadlockEdge;
		}
	}
	
	/**
	 * Creates a result composed of the given children results.
	 * The children results must be in topological order.
	 */
	public static <T> OrientationResult compose(DisjunctiveGraph graph, List<OrientationResult> children) {
		return children.size() <= 1 
			? children.stream().findAny().orElseGet(() -> success(graph))
			: new Complex(graph, children);
	}

	/** Creates a successful result. */
	public static <T> OrientationResult.Leaf success(DisjunctiveGraph graph) {
		return new Leaf(graph, true, null, null);
	}
	
	/** Creates a failure because of the given strongly connected components. */
	public static <T> OrientationResult.Leaf cycle(DisjunctiveGraph graph, List<BitSet> sccs) {
		return new Leaf(graph, false, sccs, null);
	}
	
	/** Creates a failure because of a deadlock represented by the given edge. */
	public static <T> OrientationResult.Leaf deadlock(DisjunctiveGraph graph, DisjunctiveEdge deadlockEdge) {
		return new Leaf(graph, false, null, deadlockEdge);
	}
	
}