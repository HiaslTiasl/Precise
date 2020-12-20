package it.unibz.precise.graph.disj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.graph.Successors;

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
 * If {@code disjGraph} is modified while traversing {@link Graph#successors},
 * the behavior is undefined.
 * 
 * @author MatthiasP
 */
public final class ClusteredGraph implements Graph.Lazy {
	
	//private final Set<T> nodes;
	//private final Map<T, Set<T>> succ;
	private DisjunctiveGraph disjGraph;
	private ArrayList<BitSet> groups;
	private int[] groupIndexByNode;
	
	public ClusteredGraph(DisjunctiveGraph disjGraph) {
		this.disjGraph = disjGraph;
		int nodes = disjGraph.nodes();
		groups = new ArrayList<BitSet>();
		for (DisjunctiveEdge e : disjGraph.edges()) {
			addGroup(e.getLeft());
			addGroup(e.getRight());
		}
		groupIndexByNode = new int[nodes];
		Arrays.fill(groupIndexByNode, -1);
		for (int i = 0, len = groups.size(); i < len; i++) {
			BitSet g = groups.get(i);
			for (int n = g.nextSetBit(0); n >= 0; n = g.nextSetBit(n + 1))
				groupIndexByNode[n] = i;
		}
	}

	/** Add the given group to the map. */
	private void addGroup(BitSet group) {
		for (int i = groups.size(); i-- > 0;) {
			if (group.intersects(groups.get(i)))
				group.or(groups.remove(i));
		}
		groups.add(group);
	}
	
	@Override
	public int nodes() {
		return disjGraph.nodes();
	}
	
	@Override
	public Successors successors(int node) {
		BitSet group = groups.get(node);
		
		return new Successors() {
			private int cur		= -2;		// Current node in the group
			private int curSucc	= -1;		// Current successor node
			
			@Override
			public int next() {
				if (cur == -2)
					cur = group.nextSetBit(0);
				if (cur == -1)
					return -1;
				BitSet succs = disjGraph.allSuccessors(cur);
				while (true) {
					curSucc = succs.nextSetBit(curSucc + 1);
					if (curSucc >= 0)
						return groupIndexByNode[curSucc];
					cur = group.nextSetBit(cur + 1);
					if (cur < 0)
						return -1;
				}
			}
		};
	}
}