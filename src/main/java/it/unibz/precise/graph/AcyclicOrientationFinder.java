package it.unibz.precise.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;

/**
 * Searches an acyclic orientation of a given {@link DisjunctiveGraph}.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of nodes in the {@link DisjunctiveGraph}
 */
public class AcyclicOrientationFinder<T> {
	
	/**
	 * Represents the result of the resolve operation of a disjunctive edge.
	 * 
	 * @author MatthiasP
	 *
	 */
	public enum ResolveType {
		PROBLEM,	// a problem was found (currently not used)
		NONE,		// no disjunctive edges were resolved
		OTHERS,		// other disjunctive edges were resolved	
		TARGET;		// the target edge was resolved
		
		/**
		 * Indicates whether this result can be immediately returned without further
		 * trying to resolve the target edge.
		 */
		public boolean isExitEarly() {
			switch (this) {
			case PROBLEM: 
			case TARGET:
				return true;
			default:
				return false;
			}
		}
		
		/**
		 * Indicates whether the operation was successful, in the sense that at least
		 * some edges have been resolved.
		 */
		public boolean isSuccess() {
			switch (this) {
			case OTHERS:
			case TARGET:
				return true;
			default:
				return false;
			}
		}
		
		/** Merges the results of two attempts to resolve an edge. */
		public ResolveType merge(ResolveType that) {
			return this.isExitEarly() ? this
				: that.isExitEarly() ? that
				: this.isSuccess() ? this : that;
		}
		
	}
	
	private DisjunctiveGraph<T> initialGraph;			// The graph to be resolved
	private SCCTarjan sccFinder;

	private ArrayList<DisjunctiveEdge<T>> safeEdges;	// List of disjunctive edges, allows to iterate over them without ConcurrentModificationException
	private ArrayDeque<T> queue;						// Queue of discovered nodes to visit next
	private Map<T, T> discoveredStartingFrom;			// Maps nodes discovered during resolving an edge to the corresponding start node
	
	private DisjunctiveEdge<T> firstUnresolvedEdge;		// Kept to exit early when considering the same edge twice without resolving any edges in between
	
	/** Creates a new {@code AcyclicOrientationFinder} and initializes it with the given graph. */
	public AcyclicOrientationFinder(DisjunctiveGraph<T> initialGraph) {
		this.initialGraph = initialGraph;
		this.safeEdges = new ArrayList<>(initialGraph.edges());
		this.sccFinder = new SCCTarjan();
		this.queue = new ArrayDeque<>();
		this.discoveredStartingFrom = new HashMap<>();			// capacity / load factor
	}
	
	/**
	 * Attempts to orient the graph and returns the result.
	 * Mutates the graph in the process.
	 * Resolves disjunctive edges to arcs until no more edges can be resolved
	 * or a cycle is introduced.
	 * If a cycle is introduced, a result indicating this error is returned.
	 * Otherwise, if all edges were resolved, a successful result is returned.
	 * Otherwise, some edge is chosen and both directions are tried out.
	 */
	public Result<T> search() {
		return search(initialGraph);
	}
	
	/** Recursive implementation of {@link #search()}. */
	private Result<T> search(DisjunctiveGraph<T> graph) {
		List<List<T>> nonTrivialSCCs = tryResolvingAllEdges(graph);
		// TODO: Think of better heuristic for choosing an edge here,
		//       and also for deciding which direction to try out first,
		//       both for finding a direction here and resolving edges in
		//       the call above.
		return !nonTrivialSCCs.isEmpty() ? Result.error(graph, nonTrivialSCCs)
			: firstUnresolvedEdge == null 					// Equivalent to graph.edges().isEmpty() 
				? Result.success(graph)
				: findDirection(graph, firstUnresolvedEdge);		// Recursion (indirect)
	}
	
	/** Attempts to find an acyclic orientation by trying out both directions of the given edge. */
	private Result<T> findDirection(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> e) {
		Set<T> left = e.getLeft(), right = e.getRight();
		
		Result<T> rs = tryDirection(graph, e, left, right);
		if (rs.isSuccess())
			return rs;
		
		// N.B: We probably resolved some edges, but it did not work.
		// Before trying the other direction, we need to reset the list of edges.
		safeEdges.clear();
		safeEdges.addAll(graph.edges());

		rs = tryDirection(graph, e, right, left);
		if (rs.isSuccess())
			return rs;
		
		return Result.error(graph, e);
	}
	
	/** Returns the result of searching an acyclic orientation of the graph where the given direction of the given edge. */
	private Result<T> tryDirection(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> e, Set<T> from, Set<T> to) {
		DisjunctiveGraph<T> copy = new DisjunctiveGraph<>(graph);
		copy.orient(e, from, to);
		return search(copy);												// Recursion
	}
	
	/**
	 * Attempts to resolve edges until either no more edges can be resolved or a cycle is introduced.
	 * Returns a list of the resulting non-trivial strongly connected components.
	 */
	private List<List<T>> tryResolvingAllEdges(DisjunctiveGraph<T> graph) {
		List<List<T>> nonTrivialSCCs = detectCycles(graph);
		firstUnresolvedEdge = null;
		boolean resolvedAny = true;
		while (resolvedAny && nonTrivialSCCs.isEmpty()) {
			resolvedAny = false;
			// Iterating in reverse order to simplify removing elements on the way
			for (int i = safeEdges.size() - 1; i >= 0; i--) {
				DisjunctiveEdge<T> e = safeEdges.get(i);
				// Remove edges already resolved in previous iterations to never consider them again
				if (!graph.edges().contains(e))
					safeEdges.remove(i);
				else {
					ResolveType result = tryResolving(graph, e);
					if (result.isSuccess()) {
						resolvedAny = true;
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
			
			if (resolvedAny)
				nonTrivialSCCs = detectCycles(graph);
		}
		
		return nonTrivialSCCs;
	}
	
	/** Returns non-trivial strongly connected components in the given disjunctive graph. */
	private List<List<T>> detectCycles(DisjunctiveGraph<T> graph) {
		return sccFinder.findNonTrivialSCCs(graph).collect(Collectors.toList());
	}
	
	/** Attempts to resolve the given edge. */
	private ResolveType tryResolving(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> e) {
		ResolveType leftToRight = iterativeBFS(graph, e.getLeft(), e.getRight());
		if (leftToRight.isExitEarly())
			return leftToRight;
		ResolveType rightToLeft = iterativeBFS(graph, e.getRight(), e.getLeft());
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
	private ResolveType iterativeBFS(DisjunctiveGraph<T> graph, Set<T> startNodes, Set<T> targets) {
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
	
	/**
	 * Represents the result of searching an acyclic orientation.
	 * Indicates a success or an error.
	 * An error is either indicated by a strongly connected components
	 * or a disjunctive edge that cannot be oriented without introducing
	 * cycles.
	 */
	public static class Result<T> {
		
		private boolean success;
		private DisjunctiveGraph<T> graph;
		private List<List<T>> sccs;
		private DisjunctiveEdge<T> problematicEdge;
		
		private Result(boolean success, DisjunctiveGraph<T> graph, List<List<T>> sccs, DisjunctiveEdge<T> problematicEdge) {
			this.success = success;
			this.graph = graph;
			this.sccs = sccs;
			this.problematicEdge = problematicEdge;
		}
		
		/** Creates a successful result. */
		public static <T> Result<T> success(DisjunctiveGraph<T> graph) {
			return new Result<>(true, graph, null, null);
		}
		
		/** Creates a failure because of the given strongly connected components. */
		public static <T> Result<T> error(DisjunctiveGraph<T> graph, List<List<T>> sccs) {
			return new Result<>(false, graph, sccs, null);
		}
		
		/** Creates a failure because of the given problematic disjunctive edge. */
		public static <T> Result<T> error(DisjunctiveGraph<T> graph, DisjunctiveEdge<T> problematicEdge) {
			return new Result<>(false, graph, null, problematicEdge);
		}

		public boolean isSuccess() {
			return success;
		}
		
		public DisjunctiveGraph<T> getGraph() {
			return graph;
		}
		
		public List<List<T>> getSccs() {
			return sccs;
		}
		
		public DisjunctiveEdge<T> getProblematicEdge() {
			return problematicEdge;
		}
		
	}
	
}
