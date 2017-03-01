package it.unibz.precise.graph.disj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Searches an acyclic orientation of a given {@link DisjunctiveGraph}.
 * 
 * @author MatthiasP
 *
 */
@Service
public class AcyclicOrientationFinder {
	
	@Autowired
	private DisjunctiveGraphPartitioner partitioner;
	
	@Autowired
	private SimpleDisjunctiveGraphCycleDetector simpleCycleDetector;
	
	@Autowired
	private DisjunctiveGraphResolver resolver;
	
	/**
	 * Initializes the operation with the given settings.
	 * 
	 * @param partitioning If {@literal true}, a {@link DisjunctiveGraphPartitioner} is
	 *        used to partition graphs into subgraphs before searching for acyclic
	 *        orientations, and before resolving.
	 * @param resolving If {@literal true}, a {@link DisjunctiveGraphResolver} is used
	 *        to simplify the graph and its subgraphs.
	 *        Otherwise, only a {@link SimpleDisjunctiveGraphCycleDetector} is used to
	 *        detect (less) cycles.
	 *        Otherwise, {@literal null} is passed instead and no partitioning occurs.
	 * @return An instantiated AcyclicOrientationFinderOperation that is ready to start
	 *         searching.
	 */
	public <T> AcyclicOrientationFinderOperation init(boolean partitioning, boolean resolving) {
		return new AcyclicOrientationFinderOperation(
			partitioning ? partitioner : null,
			resolving ? resolver : simpleCycleDetector
		);
	}
	
	/**
	 * Shortcut for instantiating and starting an {@link AcyclicOrientationFinderOperation}
	 * that uses both {@link DisjunctiveGraphResolver resolving} and
	 * {@link DisjunctiveGraphPartitioner partitioning}.
	 */
	public <T> OrientationResult<T> search(DisjunctiveGraph<T> graph) {
		return init(true, true).search(graph);
	}
	
}
