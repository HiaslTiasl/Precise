package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	 * @param sccSupplier
	 * @return a list of list of tasks, corresponding to all SCCs in {@code graph}.
	 */
	<T, SCC extends Collection<T>> List<SCC> findSCCs(Graph<T> graph, Supplier<SCC> sccSupplier);
	
	/**
	 * Return a list of SCCs in {@code graph}.
	 * @param graph The graph to be analyzed.
	 * @return a list of list of tasks, corresponding to all SCCs in {@code graph}.
	 */
	default <T> List<? extends List<T>> findSCCs(Graph<T> graph) {
		return findSCCs(graph, ArrayList::new);
	}

	
	/**
	 * Returns a stream of non-trivial strongly connected components.
	 * @see SCCFinder#findSCCs(Graph)
	 * @see #isNonTrivialComponent(List)
	 */
	default <T> Stream<List<T>> findNonTrivialSCCs(Graph<T> graph) {
		return findNonTrivialSCCs(graph, ArrayList::new);
	}
	
	/**
	 * Returns a stream of non-trivial strongly connected components.
	 * @see SCCFinder#findSCCs(Graph)
	 * @see #isNonTrivialComponent(List)
	 */
	default <T, SCC extends Collection<T>> Stream<SCC> findNonTrivialSCCs(Graph<T> graph, Supplier<SCC> sccSupplier) {
		return findSCCs(graph, sccSupplier).stream().filter(SCCFinder::isNonTrivialComponent);
	}

	/** Indicates whether the given SCC is trivial, i.e. whether it contains a single element only. */
	static <T, SCC extends Collection<T>> boolean isNonTrivialComponent(SCC scc) {
		return scc.size() > 1;
	}
}
