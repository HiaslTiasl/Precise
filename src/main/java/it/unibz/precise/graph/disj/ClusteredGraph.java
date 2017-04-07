package it.unibz.precise.graph.disj;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import it.unibz.precise.graph.Graph;
import it.unibz.util.Util;

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
	
	//private final Set<T> nodes;
	//private final Map<T, Set<T>> succ;
	private DisjunctiveGraph<T> disjGraph;
	private Map<T, Set<Set<T>>> groupsByNode;	// Map from nodes to the groups in which they are contained.
//	private Map<T, Set<T>> inSameGroupAs;		// Alternative where all groups of a node get merged to a single set.
	
	public ClusteredGraph(DisjunctiveGraph<T> disjGraph) {
		this.disjGraph = disjGraph;
		groupsByNode = new HashMap<>();
		Function<T, Set<Set<T>>> setSupplier = k -> new HashSet<>();
		for (DisjunctiveEdge<T> e : disjGraph.edges()) {
			addGroup(e.getLeft(), setSupplier);
			addGroup(e.getRight(), setSupplier);
		}
	}

	/** Add the given group to the map. */
	private void addGroup(Set<T> nodes, Function<T, Set<Set<T>>> setSupplier) {
		for (T n : nodes)
			groupsByNode.computeIfAbsent(n, setSupplier).add(nodes);
	}
	
	@Override
	public Collection<T> nodes() {
		return disjGraph.nodes();
	}
	
	@Override
	public Stream<T> successors(T node) {
		Set<Set<T>> groups = groupsByNode.get(node);
		Stream<T> successors = disjGraph.successors(node);
		
		return !Util.hasElements(groups) ? successors
			: Stream.concat(
				successors,
				groups.stream()
					.flatMap(Set::stream)
					.filter(n -> !node.equals(n))
				);
	}
	
}