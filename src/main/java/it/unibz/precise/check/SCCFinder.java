package it.unibz.precise.check;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import it.unibz.precise.graph.Graph;

/**
 * Finds Strongly Connected Components (SCC) in a graph.
 * 
 * Overloaded methods allow to pass in a custom {@link Supplier} to control the
 * particular {@link Collection} class used to represent SCCs.
 * 
 * @author MatthiasP
 * @see <a href="https://en.wikipedia.org/wiki/Strongly_connected_component">https://en.wikipedia.org/wiki/Strongly_connected_component</a>
 *
 */
public interface SCCFinder {
	
	/**
	 * Return a list of SCCs in {@code graph}.
	 * @param graph The graph to be analyzed.
	 * @return a list of list of tasks, corresponding to all SCCs in {@code graph}.
	 */
	Components findSCCs(Graph graph);
	
	/**
	 * Returns a stream of non-trivial strongly connected components.
	 * @see SCCFinder#findSCCs(Graph)
	 */
	default Stream<BitSet> findNonTrivialSCCs(Graph graph) {
		return Arrays.stream(findSCCs(graph).asBitSets())
			.filter(scc -> isNonTrivialComponent(scc));
	}
	
	/** Indicates whether the given SCC is non-trivial, i.e. whether contains > 1 nodes or >= 1 arcs. */
	static boolean isNonTrivialComponent(BitSet scc) {
		int size = scc.cardinality();
		if (size > 1)
			return true;
		else {
			int node = scc.nextSetBit(0);
			return scc.get(node);
		}
	}
	
	interface Components {

		int count();
		
		int componentOfNode(int node);
		
		BitSet[] asBitSets();
	}
}
