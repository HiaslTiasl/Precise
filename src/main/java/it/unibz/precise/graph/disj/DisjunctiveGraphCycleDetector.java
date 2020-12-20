package it.unibz.precise.graph.disj;

import java.util.BitSet;
import java.util.List;

public interface DisjunctiveGraphCycleDetector {

	List<BitSet> detect(DisjunctiveGraph disjGraph);
	
}
