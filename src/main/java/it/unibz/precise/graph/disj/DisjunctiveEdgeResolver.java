package it.unibz.precise.graph.disj;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.SCCFinder;

/**
 * Resolves as many {@link DisjunctiveEdge} as possible in a given {@link DisjunctiveGraph}. 
 * 
 * @author MatthiasP
 *
 */
@Service
public class DisjunctiveEdgeResolver {
	
	@Autowired
	private SCCFinder sccFinder;
	
	/** Initializes the operation. */
	public <T> DisjunctiveEdgeResolverOperation<T> init(DisjunctiveGraph<T> graph) {
		return new DisjunctiveEdgeResolverOperation<>(graph, sccFinder);
	}
	
	/** Resolves as many {@link DisjunctiveEdge} as possible in a given {@link DisjunctiveGraph}. */
	public <T> List<List<T>> resolve(DisjunctiveGraph<T> disjGraph) {
		return init(disjGraph).resolve();
	}

}
