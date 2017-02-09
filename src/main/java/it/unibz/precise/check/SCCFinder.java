package it.unibz.precise.check;

import java.util.List;
import java.util.stream.Stream;

import it.unibz.precise.graph.Graph;

/**
 * Finds Strongly Connected Components (SCC) in a graph.
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
	<T> List<List<T>> findSCCs(Graph<T> graph);
	
	/**
	 * Returns a stream of non-trivial strongly connected components.
	 * @see SCCFinder#findSCCs(Graph)
	 * @see #isNonTrivialComponent(List)
	 */
	default <T> Stream<List<T>> findNonTrivialSCCs(Graph<T> graph) {
		return findSCCs(graph).stream().filter(SCCFinder::isNonTrivialComponent);
	}

	/** Indicates whether the given SCC is trivial, i.e. whether it contains a single element only. */
	static <T> boolean isNonTrivialComponent(List<T> scc) {
		return scc.size() > 1;
	}
}
