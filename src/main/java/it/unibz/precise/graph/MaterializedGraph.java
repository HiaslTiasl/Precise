package it.unibz.precise.graph;

import java.util.BitSet;
import java.util.Set;

/**
 * A graph maintaining a {@link Set} of successor nodes for each node.
 * 
 * @author MatthiasP
 *
 */
public final class MaterializedGraph implements Graph.Eager {

	private final BitSet[] adj;

	public MaterializedGraph(BitSet[] adj) {
		this.adj = adj;
	}

	@Override
	public int nodes() {
		return adj.length;
	}

	/** Returns the successors of {@code node}. */
	public BitSet allSuccessors(int node) {
		return adj[node];
	}

	/** Creates a {@code MaterializedGraph} of the given map representing an adjacency list. */
	public static MaterializedGraph of(BitSet[] adj) {
		return new MaterializedGraph(adj);
	}
}
