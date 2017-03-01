package it.unibz.precise.graph.disj;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Resolves as many {@link DisjunctiveEdge} as possible in a given {@link DisjunctiveGraph}. 
 * 
 * @author MatthiasP
 *
 */
@Service
public class DisjunctiveGraphResolver implements DisjunctiveGraphCycleDetector {
	
	@Autowired
	private SimpleDisjunctiveGraphCycleDetector simpleCycleDetector;
	
	/** Initializes the operation. */
	public <T> DisjunctiveGraphResolverOperation<T> init(DisjunctiveGraph<T> graph) {
		return new DisjunctiveGraphResolverOperation<>(graph, simpleCycleDetector);
	}
	
	/** Resolves as many {@link DisjunctiveEdge} as possible in a given {@link DisjunctiveGraph}. */
	public <T> List<List<T>> detect(DisjunctiveGraph<T> disjGraph) {
		return init(disjGraph).resolve();
	}

}
