package it.unibz.precise.graph.disj;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Searches an acyclic orientation of a given {@link DisjunctiveGraph},
 * using a given {@link DisjunctiveGraphCycleDetector} and optionally also
 * a {@link DisjunctiveGraphPartitioner}.
 * 
 * @author MatthiasP
 *
 */
public class AcyclicOrientationFinderOperation {
	
	private DisjunctiveGraphPartitioner partitioner;
	private DisjunctiveGraphCycleDetector cycleDetector;
	
	/**
	 * Constructs a new operation with the given partitioner and cycle detector.
	 * The partitioner can be null, whereas the cycle detector cannot.
	 * If the partitioner is null, no partitioning occurs.
	 */
	public AcyclicOrientationFinderOperation(DisjunctiveGraphPartitioner partitioner, DisjunctiveGraphCycleDetector cycleDetector) {
		this.partitioner = partitioner;
		this.cycleDetector = cycleDetector;
	}
	
	/**
	 * Attempts to find an acyclic orientation of the given graph and returns a
	 * {@link OrientationResult lazy result}.
	 * If a {@link DisjunctiveGraphPartitioner} is set, the graph is first partitioned
	 * to narrow down the potential problems to smaller subgraphs.
	 * Then, the given {@link DisjunctiveGraphCycleDetector} is used to detect cycles
	 * in each resulting subgraph, or in the whole graph if it was not partitioned.
	 * If a cycle is detected, a result indicating this error is returned.
	 * Otherwise, if all edges were resolved, a successful result is returned.
	 * Otherwise, some randomly chosen edge is is tried out in both directions recursively.
	 * If a successful result is thus found, it is returned.
	 * Otherwise the edge represents a deadlock, and a corresponding failure result
	 * is returned.
	 */
	public <T> OrientationResult<T> search(DisjunctiveGraph<T> graph) {
		return partitioner != null ? partitionAndSearch(graph) : searchInPartition(graph);
	}
	
	/**
	 * Partitions the given {@link DisjunctiveGraph} into smaller subgraphs, if possible,
	 * and detects problems in each subgraph separately.
	 * If two or more subgraphs result from partitioning, the returned result is
	 * {@link OrientationResult.Complex complex}.
	 */
	private <T> OrientationResult<T> partitionAndSearch(DisjunctiveGraph<T> graph) {
		return OrientationResult.compose(
			graph,
			partitioner.orderedPartition(graph)
				.map(this::searchInPartition)
				.collect(Collectors.toList())
		);
	}
	
	/** Searches an acyclic orientation in a given partition. */
	private <T> OrientationResult<T> searchInPartition(DisjunctiveGraph<T> graph) {
		if (graph.nodes().size() <= 1) {
			// Graph has only a single node.
			// If there is a self-loop on that node, we found a cycle.
			// Otherwise, it is already an acyclic orientation.
			return graph.nodes().stream()
				.findAny()
				.filter(n -> graph.successorSet(n).contains(n))
				.map(n -> OrientationResult.cycles(graph, Arrays.asList(Arrays.asList(n))))
				.orElseGet(() -> OrientationResult.success(graph));
		}
		
		List<List<T>> nonTrivialSCCs = cycleDetector.detect(graph);
		
		return !nonTrivialSCCs.isEmpty()
			? OrientationResult.cycles(graph, nonTrivialSCCs)
			: processUnresolvedEdges(graph);
	}
	
	/**
	 * Processes the remaining edges in the given graph after resolving.
	 * If there are no remaining edges, a successful result is returned.
	 * Otherwise, some randomly chosen edge is tried in both directions.
	 * Then the first successful result found in one of the two directions
	 * is returned, otherwise a failure is returned. 
	 */
	private <T> OrientationResult<T> processUnresolvedEdges(DisjunctiveGraph<T> graph) {
		return graph.edges().stream()
			.findAny()
			.map(e -> findDirection(graph, e))
			.orElseGet(() -> OrientationResult.success(graph));
	}
	
	/** Attempts to find an acyclic orientation by trying out both directions of the given edge. */
	private <T> OrientationResult<T> findDirection(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> e) {
		Set<T> left = e.getLeft(), right = e.getRight();
		
		OrientationResult<T> rs = tryDirection(graph, e, left, right);
		if (rs.isSuccessful())
			return rs;

		rs = tryDirection(graph, e, right, left);
		if (rs.isSuccessful())
			return rs;
		
		return OrientationResult.deadlock(graph, e);
	}
	
	/** Returns the result of searching an acyclic orientation of the graph with the given direction of the given edge. */
	private <T> OrientationResult<T> tryDirection(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> e, Set<T> from, Set<T> to) {
		DisjunctiveGraph<T> copy = DisjunctiveGraph.copySealedNodes(graph);
		copy.orient(e, from, to);
		return search(copy);												// Recursion
	}
	
}
