package it.unibz.precise.graph.disj;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link DisjunctiveGraphResolver}, encapsulating the needed data structures
 * for one search operation.
 * 
 * @author MatthiasP
 */
public class DisjunctiveGraphResolverOperation {
	
	private SimpleDisjunctiveGraphCycleDetector simpleCycleDetector;

	private DisjunctiveGraph graph;			// The graph to be resolved

	// TODO consider using a single BitSet, deviating from BFS
	private ArrayDeque<BitSet> queue;			// Queue of discovered nodes to visit next
	// TODO consider using a single BitSet, not sure why we needed the Map value
	private int[] discoveredStartingFrom;		// Maps nodes discovered during resolving an edge to the corresponding start node
	
	public DisjunctiveGraphResolverOperation(DisjunctiveGraph graph, SimpleDisjunctiveGraphCycleDetector simpleCycleDetector) {
		this.graph = graph;
		this.simpleCycleDetector = simpleCycleDetector;
	}

	/**
	 * Attempts to resolve edges until either no more edges can be resolved or a cycle is introduced.
	 * Returns a list of the resulting non-trivial strongly connected components.
	 */
	public List<BitSet> resolve() {
		List<BitSet> nonTrivialSCCs = simpleCycleDetector.detect(graph);
		// Kept to exit early when considering the same edge twice without resolving any edges in between
		DisjunctiveEdge firstUnresolvedEdge = null;
		boolean again = nonTrivialSCCs.isEmpty();
		
		if (again) {
			// Lazy init of data structures
			queue = new ArrayDeque<>();
			discoveredStartingFrom = new int[graph.nodes()];
		}
		while (again) {
			again = false;
			// Iterating in reverse order to simplify removing elements on the way
			for (Iterator<DisjunctiveEdge> it = graph.edges().iterator(); it.hasNext();) {
				DisjunctiveEdge e = it.next();
				if (tryResolving(e)) {
					again = true;
					firstUnresolvedEdge = null;
					// Remove this edge if it was resolved in this iteration, using iterator
					it.remove();
				}
				else if (firstUnresolvedEdge == e)
					break;
				else if (firstUnresolvedEdge == null)
					firstUnresolvedEdge = e;
			}
			
			if (again) {
				nonTrivialSCCs = simpleCycleDetector.detect(graph);
				again = nonTrivialSCCs.isEmpty();
			}
		}
		
		return nonTrivialSCCs;
	}
	
	
	/** Attempts to resolve the given edge. */
	private boolean tryResolving(DisjunctiveEdge e) {
		BitSet left = e.getLeft(), right = e.getRight();
		return iterativeBFS(left, right)
			|| iterativeBFS(right, left);
	}
	
	/**
	 * Attempts to resolve a disjunctive edge in the given direction in a breadth first manner,
	 * and indicates whether this was possible to do.
	 */
	private boolean iterativeBFS(BitSet startNodes, BitSet targets) {
		// Reset queue and map, put startNodes into queue
		queue.clear();
		queue.add((BitSet)startNodes.clone());
		Arrays.fill(discoveredStartingFrom, -1);
		// TODO inner loop on BitSet from queue
		while (!queue.isEmpty()) {
			int current = poll();
			int start = discoveredStartingFrom[current];
			if (start < 0) {								// Starting node -> put it into the map on the fly
				start = current;
				discoveredStartingFrom[current] = start;
			}
			else if (targets.get(current)) {
				// N.B: We cannot use graph.orient, because it would lead to a ConcurrentModificationException.
				// Therefore, we manually add arcs and use the iterator to remove the resolved edge.
				graph.addAllArcs(startNodes, targets);
				return true;
			}
			BitSet successors = graph.allSuccessors(current);
			// Add new nodes to queue and map
			for (int succ = successors.nextSetBit(0); succ >= 0; succ = successors.nextSetBit(succ + 1)) {
				if (discoveredStartingFrom[succ] < 0) {
					discoveredStartingFrom[succ] = start;
					offer(succ);
				}
			}
		}
		return false;
	}

	private int poll() {
		BitSet chunk = queue.peek();
		int node = chunk.nextSetBit(0);
		chunk.clear(node);
		if (chunk.isEmpty())
			queue.poll();
		return node;
	}

	private void offer(int node) {
		BitSet chunk = queue.peekLast();
		int lastNode = chunk.previousSetBit(graph.nodes() - 1);
		if (lastNode < node)
			chunk.set(node);
		else {
			BitSet newChunk = new BitSet();
			newChunk.set(node);
			queue.offer(newChunk);
		}
	}
}
