package it.unibz.precise.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibz.precise.check.SCCFinder;
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
	
//	private Set<T> discovered;
//	private Set<T> entered;
//	private Set<T> exited;
	private DisjunctiveGraph<T> graph;					// The graph to be resolved
	private SCCTarjan sccFinder;

	private Map<T, T> discoveredStartingFrom;			// Maps nodes discovered during resolving an edge to the corresponding start node
	private DisjunctiveEdge<T> firstUnresolvedEdge;		// Kept to exit early when considering the same edge twice without resolving any edges in between
	
	public AcyclicOrientationFinder(DisjunctiveGraph<T> graph) {
		this.graph = graph;
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
		int nodeCount = graph.nodes().size();
//		discovered = new HashSet<>(nodeCount);
//		entered = new HashSet<>(nodeCount);
//		exited = new HashSet<>(nodeCount);
		discoveredStartingFrom = new HashMap<>(nodeCount);
		sccFinder = new SCCTarjan();
		
		List<List<T>> nonTrivialSCCs = tryResolvingAllEdges();
		
		// TODO: Think of a better heuristic for choosing an edge
		return !nonTrivialSCCs.isEmpty() ? Result.error(graph, nonTrivialSCCs)
			: firstUnresolvedEdge == null 					// Equivalent to graph.edges().isEmpty() 
				? Result.success(graph)
				: findDirection(firstUnresolvedEdge);		// Recursion (indirect)
	}
	
	/** Attempts to find an acyclic orientation by trying out both directions of the given edge. */
	private Result<T> findDirection(DisjunctiveEdge<T> e) {
		// TODO: Think of a better heuristic for choosing which direction to try out first
		Set<T> left = e.getLeft(), right = e.getRight();
		System.out.println("Try direction, left to right");
		Result<T> rs = tryDirection(e, left, right);
		if (rs.isSuccess())
			return rs;
		System.out.println("Try direction, right to left");
		rs = tryDirection(e, right, left);
		if (rs.isSuccess())
			return rs;
		return Result.error(graph, e);
	}
	
	/** Returns the result of searching an acyclic orientation of the graph where the given direction of the given edge. */
	private Result<T> tryDirection(DisjunctiveEdge<T> e, Set<T> from, Set<T> to) {
		DisjunctiveGraph<T> copy = new DisjunctiveGraph<>(graph);
		copy.orient(e, from, to);
		return new AcyclicOrientationFinder<>(copy).search();			// Recursion
	}
	
	/**
	 * Attempts to resolve edges until either no more edges can be resolved or a cycle is introduced.
	 * Returns a list of the resulting non-trivial strongly connected components.
	 */
	private List<List<T>> tryResolvingAllEdges() {
		List<List<T>> nonTrivialSCCs;
		boolean resolvedAny;
		// Copy edges into a new list to avoid ConcurrentModificationException
		ArrayList<DisjunctiveEdge<T>> safeEdges = new ArrayList<>(graph.edges());
		do {
			resolvedAny = false;
			// Iterating in reverse order to simplify removing elements on the way
			for (int i = safeEdges.size() - 1; i >= 0; i--) {
				DisjunctiveEdge<T> e = safeEdges.get(i);
				// Remove edges already resolved in previous iterations to never consider them again
				if (!graph.edges().contains(e))
					safeEdges.remove(i);
				else {
					ResolveType result = tryResolving(e);
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
			System.out.println("Edges: " + graph.edges().size());
			// Detect cycles
			nonTrivialSCCs = sccFinder.findSCCs(graph).stream()
				.filter(SCCFinder::isNonTrivialComponent)
				.collect(Collectors.toList());
		} while (resolvedAny && nonTrivialSCCs.isEmpty());
		
		return nonTrivialSCCs;
	}
	
	/** Attempts to resolve the given edge,  */
	private ResolveType tryResolving(DisjunctiveEdge<T> e) {
		// TODO: Think of a better heuristic for what direction to try out first
		ResolveType leftToRight = iterativeBFS(e.getLeft(), e.getRight());
		if (leftToRight.isExitEarly())
			return leftToRight;
		ResolveType rightToLeft = iterativeBFS(e.getRight(), e.getLeft());
		return leftToRight.merge(rightToLeft);
	}
	
//	private ResolveType tryResolving(Set<T> from, Set<T> to) {
//		ResolveType resolved = ResolveType.NONE;
//		discovered.clear();
//		for (T n : from) {
//			ResolveType rt = iterativeDFS(n, to);
//			resolved = resolved.merge(rt);
//			if (resolved.isExitEarly())
//				break;
//		}
//		return resolved;
//	}
	
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
		ArrayDeque<T> queue = new ArrayDeque<>(startNodes);
		discoveredStartingFrom.clear();							// Reuse map for multiple calls for saving memory
		while (!queue.isEmpty()) {
			T current = queue.poll();
			T start = discoveredStartingFrom.get(current);
			if (start == null) {								// Starting node -> put it into the map
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
	
	// Old implementations using other traversal strategies
	
//	private ResolveType iterativeDFS(T start, Set<T> targets) {
//		if (discovered.contains(start))
//			return ResolveType.NONE;
//		ResolveType resolved = ResolveType.NONE;
//		ArrayDeque<T> stack = new ArrayDeque<>();
//		stack.add(start);
//		discovered.add(start);
//		while (!stack.isEmpty()) {
//			T current = stack.pop();
//			if (graph.orient(start, current))
//				resolved = ResolveType.SOME;
//			if (targets.contains(current))
//				return ResolveType.TARGET;
//			for (T succ : graph.successorSet(current)) {
//				if (!discovered.contains(succ)) {
//					discovered.add(succ);
//					stack.push(succ);
//				}
//			}
//		}
//		return resolved;
//	}
//	
//	private ResolveType recursiveDFS(T start, Set<T> targets) {
//		if (discovered.contains(start))
//			return ResolveType.NONE;
//		entered.clear();
//		exited = discovered;
//		return recursiveDFS(start, start, targets);
//	}
//	
//	private ResolveType recursiveDFS(T start, T current, Set<T> targets) {
//		if (targets.contains(current))
//			return ResolveType.TARGET;
//		if (entered.contains(current))
//			return ResolveType.PROBLEM;
//		ResolveType resolved = ResolveType.NONE;
//		enter(current);
//		for (T succ : safeSuccessors(current)) {
//			if (graph.orient(start, succ))
//				resolved = ResolveType.SOME;
//			if (!exited.contains(succ)) {
//				resolved = resolved.merge(recursiveDFS(start, succ, targets));
//				if (resolved.isExitEarly())
//					break;
//			}
//		}
//		exit(current);
//		return resolved;
//	}
//	
//	private void enter(T n) {
//		entered.add(n);
//	}
//	
//	private void exit(T n) {
//		entered.remove(n);
//		exited.add(n);
//	}
	
//	private List<T> safeSuccessors(T n) {
//		return new ArrayList<>(graph.successorSet(n));
//	}
	
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
