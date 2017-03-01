package it.unibz.precise.graph.disj;

import java.util.List;

public interface DisjunctiveGraphCycleDetector {

	<T> List<List<T>> detect(DisjunctiveGraph<T> disjGraph);
	
}
