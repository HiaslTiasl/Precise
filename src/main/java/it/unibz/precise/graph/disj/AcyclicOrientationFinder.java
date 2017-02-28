package it.unibz.precise.graph.disj;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.graph.SCCTarjan;

/**
 * Searches an acyclic orientation of a given {@link DisjunctiveGraph}.
 * 
 * @author MatthiasP
 *
 */
@Service
public class AcyclicOrientationFinder {
	
	@Autowired
	private DisjunctiveEdgeResolver resolver;
	
	@Autowired
	private SCCTarjan tarjan;		// N.B.: We explicitly ask for SCCTarjan to make sure that SCCs are always returned in topological order
	
	/**
	 * Attempts to find an acyclic orientation of the given graph and returns a
	 * {@link OrientationResult lazy result}.
	 * To find such an orientation, the graph is recursively partitioned into
	 * {@link ClusteredGraph clusters} to narrow down the potential problems to smaller
	 * subgraphs.
	 * Then each resulting subgraph is simplified by {@link DisjunctiveEdgeResolver resolving}
	 * disjunctive edges to directed arcs.
	 * If a cycle is introduced, a result indicating this error is returned.
	 * Otherwise, if all edges were resolved, a successful result is returned.
	 * Otherwise, some randomly chosen edge is is tried out in both directions recursively.
	 * If a successful result is thus found, it is returned.
	 * Otherwise the edge represents a deadlock, and a corresponding failure result
	 * is returned.
	 */
	public <T> OrientationResult<T> search(DisjunctiveGraph<T> graph) {
		return OrientationResult.compose(
			graph,
			partition(graph)
				.map(this::searchInPartition)
				.collect(Collectors.toList())
		);
	}
	
	/** Searches an acyclic orientation an a given partition. */
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
		
		List<List<T>> nonTrivialSCCs = resolver.resolve(graph);
		
		return !nonTrivialSCCs.isEmpty()
			? OrientationResult.cycles(graph, nonTrivialSCCs)
			: processUnresolvedEdges(graph);
	}
	
	/**
	 * Returns a parallel stream of independent subgraphs of the given graph.
	 * It is guaranteed that the stream contains all subgraphs, even if they are trivial,
	 * and that it follows a topological ordering on the subgraphs, i.e. respecting the
	 * cut arcs in {@code graph}.
	 * These guarantees are expected by {@link OrientationResult#buildOrientation()}
	 * to allow an efficient implementation and therefore must be kept. 
	 * @see ClusteredGraph
	 * @see SCCTarjan
	 */
	private <T> Stream<DisjunctiveGraph<T>> partition(DisjunctiveGraph<T> graph) {
		// Use SCCTarjan to obtain SCCs in topological order
		return tarjan.findSCCs(new ClusteredGraph<>(graph), HashSet::new).parallelStream()
			.map(graph::restrictedTo);
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
