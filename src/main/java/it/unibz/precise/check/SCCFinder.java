package it.unibz.precise.check;

import java.util.List;

import it.unibz.precise.graph.Graph;

public interface SCCFinder {
	
	<T> List<List<T>> findSCCs(Graph<T> graph);

	static <T> boolean isNonTrivialComponent(List<T> component) {
		return component.size() > 1;
	}
}
