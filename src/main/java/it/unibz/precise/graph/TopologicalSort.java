package it.unibz.precise.graph;

import java.util.List;
import java.util.stream.Collectors;

import it.unibz.precise.check.SCCFinder;

/**
 * Topologically sorts a graph.
 * The implementation uses {@link SCCTarjan} to perform the sorting.
 * 
 * @author MatthiasP
 * @see Graph
 * @see SCCTarjan
 * @param <T>
 */
public class TopologicalSort<T> {
	
	private Graph<T> graph;

	public TopologicalSort(Graph<T> graph) {
		this.graph = graph;
	}
	
	public List<T> sort() {
		List<List<T>> components = new SCCTarjan().findSCCs(graph);
		
		List<List<?>> nonTrivialSCCs = components.stream()
			.filter(SCCFinder::isNonTrivialComponent)
			.collect(Collectors.toList());
		
		if (nonTrivialSCCs.size() > 0)
			throw new CyclicGraphException(nonTrivialSCCs);
		
		return components.stream()
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}
	
	public static <T> List<T> sort(Graph<T> graph) {
		return new TopologicalSort<>(graph).sort();
	}
	
	public static class CyclicGraphException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		private List<List<?>> sccs;
		
		public CyclicGraphException(List<List<?>> sccs) {
			super("Cannot topologically sort cyclic graph");
		}

		public List<List<?>> getSccs() {
			return sccs;
		}
	}
	
}
