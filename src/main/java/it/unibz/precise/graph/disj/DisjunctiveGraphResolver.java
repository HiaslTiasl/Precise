package it.unibz.precise.graph.disj;

import java.util.BitSet;
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
	public DisjunctiveGraphResolverOperation init(DisjunctiveGraph graph) {
		return new DisjunctiveGraphResolverOperation(graph, simpleCycleDetector);
	}
	
	/** Resolves as many {@link DisjunctiveEdge} as possible in a given {@link DisjunctiveGraph}. */
	public List<BitSet> detect(DisjunctiveGraph disjGraph) {
		return init(disjGraph).resolve();
	}

}
