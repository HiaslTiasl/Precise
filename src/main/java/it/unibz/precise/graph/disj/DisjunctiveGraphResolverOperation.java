package it.unibz.precise.graph.disj;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link DisjunctiveGraphResolver}, encapsulating the needed data structures
 * for one search operation.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes in the graph
 */
public class DisjunctiveGraphResolverOperation<T> {
	
	private SimpleDisjunctiveGraphCycleDetector simpleCycleDetector;

	private DisjunctiveGraph<T> graph;					// The graph to be resolved

	private ArrayDeque<T> queue;						// Queue of discovered nodes to visit next
	private HashMap<T, T> discoveredStartingFrom;		// Maps nodes discovered during resolving an edge to the corresponding start node
	
	public DisjunctiveGraphResolverOperation(DisjunctiveGraph<T> graph, SimpleDisjunctiveGraphCycleDetector simpleCycleDetector) {
		this.graph = graph;
		this.simpleCycleDetector = simpleCycleDetector;
	}

	/**
	 * Attempts to resolve edges until either no more edges can be resolved or a cycle is introduced.
	 * Returns a list of the resulting non-trivial strongly connected components.
	 */
	public List<List<T>> resolve() {
		List<List<T>> nonTrivialSCCs = simpleCycleDetector.detect(graph);
		// Kept to exit early when considering the same edge twice without resolving any edges in between
		DisjunctiveEdge<T> firstUnresolvedEdge = null;
		boolean again = nonTrivialSCCs.isEmpty();
		
		if (again) {
			// Lazy init of data structures
			queue = new ArrayDeque<>();
			discoveredStartingFrom = new HashMap<>();
		}
		while (again) {
			again = false;
			// Iterating in reverse order to simplify removing elements on the way
			for (Iterator<DisjunctiveEdge<T>> it = graph.edges().iterator(); it.hasNext();) {
				DisjunctiveEdge<T> e = it.next();
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
	private boolean tryResolving(DisjunctiveEdge<T> e) {
		Set<T> left = e.getLeft(), right = e.getRight();
		return iterativeBFS(left, right)
			|| iterativeBFS(right, left);
	}
	
	/**
	 * Attempts to resolve a disjunctive edge in the given direction in a breadth first manner,
	 * and indicates whether this was possible to do.
	 */
	private boolean iterativeBFS(Set<T> startNodes, Set<T> targets) {
		// Reset queue and map, put startNodes into queue
		queue.clear();
		queue.addAll(startNodes);
		discoveredStartingFrom.clear();
		while (!queue.isEmpty()) {
			T current = queue.poll();
			T start = discoveredStartingFrom.get(current);
			if (start == null) {								// Starting node -> put it into the map on the fly
				start = current;
				discoveredStartingFrom.put(current, start);
			}
			else if (targets.contains(current)) {
				// N.B: We cannot use graph.orient, because it would lead to a ConcurrentModificationException.
				// Therefore, we manually add arcs and use the iterator to remove the resolved edge.
				graph.addAllArcs(startNodes, targets);
				return true;
			}
			for (T succ : graph.successorSet(current)) {		// Add new nodes to queue and map
				if (!discoveredStartingFrom.containsKey(succ)) {
					discoveredStartingFrom.put(succ, start);
					queue.offer(succ);
				}
			}
		}
		return false;
	}
	
}
