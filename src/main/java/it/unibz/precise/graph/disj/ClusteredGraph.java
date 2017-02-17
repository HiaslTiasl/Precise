package it.unibz.precise.graph.disj;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import it.unibz.precise.graph.Graph;

/**
 * View on a {@link DisjunctiveGraph} that groups nodes into clusters.
 * Each node {@code n} has a successor {@code s} iff the original graph
 * has an arc from {@code n} to {@code s} or if it has a disjunctive edge
 * between two sets of nodes such that both {@code n} and {@code s} are
 * contained in the same set.
 * <p>
 * The SCCs (clusters) of this graph correspond to an independent
 * partition of the original disjunctive graph.
 * Thus, it is guaranteed that the original graph has an acyclic orientation
 * iff each subgraph induced by a SCC of the returned graph has an acyclic
 * orientation.
 * <p>
 * If there is already a cycle, then it is fully contained in a cluster.
 * If a {@code DisjunctiveEdge} has both sides in the same cluster, it
 * can be checked independently in the corresponding subgraph.
 * Finally, if a {@code DisjunctiveEdge} spans from one cluster to another,
 * we can always resolve it since arcs from nodes of one cluster to nodes
 * of another cluster always follow the same direction, otherwise the two
 * clusters would be merged into one.
 * <p>
 * Note that the returned Graph is a view on {@code disjGraph}.
 * If {@code disjGraph} is modified while traversing {@link Graph#nodes()}
 * or {@link Graph#successors(Object)}, the behavior is undefined.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes
 */
public final class ClusteredGraph<T> implements Graph<T> {
	
	private final DisjunctiveGraph<T> disjGraph;
	
	public ClusteredGraph(DisjunctiveGraph<T> disjGraph) {
		this.disjGraph = disjGraph;
	}

	@Override
	public Collection<T> nodes() {
		return disjGraph.nodes();
	}

	@Override
	public Stream<T> successors(T node) {
		// Groups of nodes corresponding to the side of disjunctive edges that contain
		// the given node.
		Stream<Set<T>> exclusiveGroups = disjGraph.disjunctions(node).stream()
			.map(e -> e.getSide(node))
			.filter(Objects::nonNull);		// Should not be necessary
		
		return Stream.concat(
			disjGraph.successors(node),
			exclusiveGroups.flatMap(Set::stream)
		);
	}
	
}