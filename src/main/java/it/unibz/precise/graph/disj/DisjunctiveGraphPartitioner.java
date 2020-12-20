package it.unibz.precise.graph.disj;

import it.unibz.precise.check.SCCFinder;
import it.unibz.precise.graph.SCCTarjan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Partitions a {@link DisjunctiveGraph} into multiple subgraphs that can be checked independently,
 * because all arcs and edges between subgraphs can be satisfied by topologically ordering the subgraphs.
 * More precisely, it guarantees that the whole graph has an acyclic orientation iff all subgraphs
 * have one.
 * 
 * @author MatthiasP
 *
 */
@Service
public class DisjunctiveGraphPartitioner {
	
	@Autowired
	private SCCTarjan tarjan;			// N.B.: We explicitly ask for SCCTarjan to make sure that SCCs are always returned in topological order

	/**
	 * Returns a parallel stream of independent subgraphs of the given graph.
	 * It is guaranteed that the stream contains all subgraphs, even if they are trivial,
	 * and that it follows a topological ordering on the subgraphs, i.e. respecting the
	 * cut arcs in {@code graph}.
	 * These guarantees are expected by {@link OrientationResult#buildOrientation()}
	 * to allow an efficient implementation and therefore must be kept. 
	 * @see ClusteredGraph
	 * @see SCCTarjan
	 */
	public Stream<DisjunctiveGraph> orderedPartition(DisjunctiveGraph graph) {
		// Use SCCTarjan to obtain SCCs in topological order
		SCCFinder.Components clusters = tarjan.findSCCs(new ClusteredGraph(graph));
		return clusters.count() == 1 ? Stream.of(graph)
			: Arrays.stream(clusters.asBitSets()).map(graph::restrictedTo);
	}

}
