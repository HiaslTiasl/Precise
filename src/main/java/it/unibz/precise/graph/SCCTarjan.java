package it.unibz.precise.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import it.unibz.precise.check.SCCFinder;

/**
 * SCCFinder based on
 * <a href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">
 * 	Tarjan's strongly connected components algorithm
 * </a>.
 * Note that this algorithm also topologically sorts SCCs.
 * 
 * The following implementation was used as a starting point:
 * <a href="https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java">
 * 	https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java
 * </a>.
 * The biggest change from that implementation is to consider generic graphs as opposed to
 * graphs represented as integer adjacency lists.
 * This reduces the amount of converting back and forth between representations.
 * Also, the caller can pass a custom {@link Supplier} to choose the collection implementation
 * to be used to represent SCCs.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">
 * 	Tarjan's strongly connected components algorithm
 * </a>
 * @see <a href="https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java">
 * 	https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java
 * </a>
 */
@Service
public class SCCTarjan implements SCCFinder {


	public <T, SCC extends Collection<T>> List<SCC> findSCCs(Graph<T> graph, Supplier<SCC> sccSupplier) {
		return new Run<>(graph, sccSupplier).dfs();
	}
	
	/**
	 * Encapsulates one execution of {@link #findSCCs(Graph)}.
	 * 
	 * @author MatthiasP
	 *
	 * @param <T>
	 */
	private static class Run<T, SCC extends Collection<T>> {
		private Graph<T> graph;				// The graph to be checked
		private Supplier<SCC> sccSupplier;	// Supplier for creating a new, empty component
		private ArrayList<T> stack;			// Stack of nodes
		private Map<T, Integer> nodeTimes;	// Earliest time (lowest SCC index) for reaching node. Also used to mark nodes as visited.
		private List<SCC> components;		// Resulting SCCs
		private int time;					// Current time (number of visited nodes)
		
		/** Initialize the run based on {@code graph}. */
		private Run(Graph<T> graph, Supplier<SCC> sccSupplier) {
			this.graph = graph;
			this.sccSupplier = sccSupplier;
			int n = graph.nodes().size();
			stack = new ArrayList<>(n);
			nodeTimes = new HashMap<>(n);
			components = new ArrayList<>(n);
		}
		
		/** Indicates whether {@code node} was visited already. */
		private boolean visited(T node) {
			return nodeTimes.containsKey(node);
		}
		
		/** Visits {@code node}, i.e. inits its time, adds it the the stack, and increments time. */
		private void visit(T node) {
			nodeTimes.put(node, time++);
			stack.add(node);
		}
		
		/** Pops the top-most node from the stack. */
		private T pop() {
			return stack.remove(stack.size() - 1);
		}
		
		/** Start visiting nodes in depth-first order. */
		private List<SCC> dfs() {
			for (T node : graph.nodes()) {
				if (!visited(node))
					dfs(node);
			}
			// Reverse to obtain topological sort. 
			Collections.reverse(components);
			return components;
		}
		
		/**
		 * Visit {@code node} and continue visiting recursively, in depth-first order.
		 * After returning from recursion, set {@code node}'s time to the lowest reachable one.
		 * Otherwise, if the time does not need to be adjusted, there is an SCC containing {@code node}
		 * and all other nodes above it in the stack.
		 */
		private void dfs(T node) {
			visit(node);
			boolean isComponentRoot = true;
			
			// Using an Iterator instead of a Stream, because nodes are visited recursively.
			// In particular, each iteration depends on state modifications from earlier iterations.
			Iterable<T> successors = graph.successors(node)::iterator;
			for (T suc : successors) {
				if (!visited(suc))
					dfs(suc);
				int sucLow = nodeTimes.get(suc);
				if (nodeTimes.get(node) > sucLow) {
					// node can reach an earlier node -> it is not the root of the component
					nodeTimes.put(node, sucLow);
					isComponentRoot = false;
				}
			}

			if (isComponentRoot) {
				// node can reach all other nodes above it in the stack
				// -> pop all these nodes from the stack and add them as a SCC to the result.
				SCC scc = sccSupplier.get();
				T n;
				do {
					n = pop();
					scc.add(n);
					nodeTimes.put(n, Integer.MAX_VALUE);
				} while (!n.equals(node));
				components.add(scc);
			}
		}
	}

}
