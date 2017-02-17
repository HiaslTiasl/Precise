package it.unibz.precise.graph.disj;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibz.precise.check.SCCFinder;

/**
 * Implementation of {@link DisjunctiveEdgeResolver}, encapsulating the needed data structures
 * for one search operation.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes in the graph
 */
public class DisjunctiveEdgeResolverOperation<T> {
	
	private SCCFinder sccFinder;

	private DisjunctiveGraph<T> graph;			// The graph to be resolved

	private ArrayList<DisjunctiveEdge<T>> safeEdges;	// List of disjunctive edges, allows to iterate over them without ConcurrentModificationException
	private ArrayDeque<T> queue;						// Queue of discovered nodes to visit next
	private HashMap<T, T> discoveredStartingFrom;		// Maps nodes discovered during resolving an edge to the corresponding start node
	
	public DisjunctiveEdgeResolverOperation(DisjunctiveGraph<T> graph, SCCFinder sccFinder) {
		this.graph = graph;
		this.sccFinder = sccFinder;
	}

	/**
	 * Attempts to resolve edges until either no more edges can be resolved or a cycle is introduced.
	 * Returns a list of the resulting non-trivial strongly connected components.
	 */
	public List<List<T>> resolve() {
		List<List<T>> nonTrivialSCCs = detectCycles(graph);
		// Kept to exit early when considering the same edge twice without resolving any edges in between
		DisjunctiveEdge<T> firstUnresolvedEdge = null;
		boolean again = nonTrivialSCCs.isEmpty();
		
		if (again) {
			// Lazy init of data structures
			safeEdges = new ArrayList<>(graph.edges());
			queue = new ArrayDeque<>();
			discoveredStartingFrom = new HashMap<>();
		}
		while (again) {
			again = false;
			// Iterating in reverse order to simplify removing elements on the way
			for (int i = safeEdges.size() - 1; i >= 0; i--) {
				DisjunctiveEdge<T> e = safeEdges.get(i);
				// Remove edges already resolved in previous iterations to never consider them again
				if (!graph.edges().contains(e))
					safeEdges.remove(i);
				else {
					ResolveType result = tryResolving(e);
					if (result.isSuccess()) {
						again = true;
						firstUnresolvedEdge = null;
						// Remove this edge if it was resolved in this iteration
						if (result == ResolveType.TARGET)
							safeEdges.remove(i);
					}
					else {
						// Exit if there is a problem or if we arrived at some edge the second time without resolving any edges in between.
						// Note that if there is a problem, then there are SCCs,
						// and if firstUnresolvedEdge == e, then resolvedAny is false,
						// so we will also exit from the outer loop after computing SCCs.
						if (result == ResolveType.PROBLEM || firstUnresolvedEdge == e)
							break;
						else if (firstUnresolvedEdge == null)
							firstUnresolvedEdge = e;
					}
				}
			}
			
			if (again) {
				nonTrivialSCCs = detectCycles(graph);
				again = nonTrivialSCCs.isEmpty();
			}
		}
		
		return nonTrivialSCCs;
	}
	
	
	/** Attempts to resolve the given edge. */
	private ResolveType tryResolving(DisjunctiveEdge<T> e) {
		ResolveType leftToRight = iterativeBFS(e.getLeft(), e.getRight());
		if (leftToRight.isExitEarly())
			return leftToRight;
		ResolveType rightToLeft = iterativeBFS(e.getRight(), e.getLeft());
		return leftToRight.merge(rightToLeft);
	}
	
	/**
	 * Attempts to resolve a disjunctive edge in the given direction in a breadth first manner.
	 * Resolves other edges on the way.
	 * @return <ul>
	 * <li>{@link ResolveType#NONE} if no edges were resolved
	 * <li>{@link ResolveType#OTHERS} if only other edges were resolved
	 * <li>{@link ResolveType#TARGET} if the target edge was resolved
	 * </ul> 
	 */
	private ResolveType iterativeBFS(Set<T> startNodes, Set<T> targets) {
		ResolveType resolved = ResolveType.NONE;
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
			else if (graph.orient(start, current))				// Resolve any edges from start to current
				resolved = ResolveType.OTHERS;
			if (targets.contains(current))						// Reached a target node -> resolved target edge 
				return ResolveType.TARGET;
			for (T succ : graph.successorSet(current)) {		// Add new nodes to queue and map
				if (!discoveredStartingFrom.containsKey(succ)) {
					discoveredStartingFrom.put(succ, start);
					queue.offer(succ);
				}
			}
		}
		return resolved;
	}
	
	/** Returns non-trivial strongly connected components in the given disjunctive graph. */
	private List<List<T>> detectCycles(DisjunctiveGraph<T> graph) {
		return sccFinder.findNonTrivialSCCs(graph).collect(Collectors.toList());
	}

}
