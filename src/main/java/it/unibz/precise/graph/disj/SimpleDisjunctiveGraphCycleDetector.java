package it.unibz.precise.graph.disj;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.SCCFinder;

/**
 * Simple implementation of {@link DisjunctiveGraphCycleDetector} that directly checks
 * for strongly connected components without any attempt to first simplify the given
 * graph.
 * 
 * @author MatthiasP
 *
 */
@Service
public class SimpleDisjunctiveGraphCycleDetector implements DisjunctiveGraphCycleDetector {
	
	@Autowired
	private SCCFinder sccFinder;

	/** Returns non-trivial strongly connected components in the given disjunctive graph. */
	@Override
	public <T> List<List<T>> detect(DisjunctiveGraph<T> disjGraph) {
		return sccFinder.findNonTrivialSCCs(disjGraph).collect(Collectors.toList());
	}


}
