package it.unibz.precise.graph;

import java.util.ArrayList;
import java.util.BitSet;
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

	private static class SCCs implements Components {

		private final int[] nodeTimes;
		private final int maxTime;

		public SCCs(int[] nodeTimes, int maxTime) {
			this.nodeTimes = nodeTimes;
			this.maxTime = maxTime;
		}

		@Override
		public int count() {
			return maxTime;
		}

		@Override
		public int componentOfNode(int n) {
			return nodeTimes[n];
		}

		@Override
		public BitSet[] asBitSets() {
			BitSet[] result = new BitSet[maxTime];
			for (int n = 0; n < nodeTimes.length; n++) {
				int t = nodeTimes[n];
				BitSet c = result[t];
				if (c == null)
					c = result[t] = new BitSet();
				c.set(n);
			}
			return result;
		}
		
	}

	public Components findSCCs(Graph graph) {
		return new Run(graph).dfs();
	}
	
	/**
	 * Encapsulates one execution of {@link #findSCCs(Graph)}.
	 * 
	 * @author MatthiasP
	 */
	private static class Run {
		private Graph graph;				// The graph to be checked
		//private ArrayList<Integer> stack;	// Stack of nodes
		private int[] nodeTimes;			// Earliest time (lowest SCC index) for reaching node. Also used to mark nodes as visited.
		//private List<BitSet> components;	// Resulting SCCs
		private int components;				// Resulting SCCs
		private int time;					// Current time (number of visited nodes)
		
		/** Initialize the run based on {@code graph}. */
		private Run(Graph graph) {
			this.graph = graph;
			int N = graph.nodes();
			//stack = new ArrayList<>(N);
			nodeTimes = new int[N];
			//components = new ArrayList<>(N);
		}
		
		/** Indicates whether {@code node} was visited already. */
		private boolean visited(int node) {
			return nodeTimes[node] > 0;
		}
		
		/** Visits {@code node}, i.e. inits its time, adds it the the stack, and increments time. */
		private void visit(int node) {
			nodeTimes[node] = ++time;
			//stack.add(node);
		}
		
//		/** Pops the top-most node from the stack. */
//		private int pop() {
//			return stack.remove(stack.size() - 1);
//		}
		
		/** Start visiting nodes in depth-first order. */
		private Components dfs() {
			int N = graph.nodes();
			for (int n = 0; n < N; n++) {
				if (!visited(n))
					dfs(n);
			}
			
			// Reverse to obtain topological sort. 
			//Collections.reverse(components);
			//return components;
			return new SCCs(nodeTimes, components);
		}
		
		/**
		 * Visit {@code node} and continue visiting recursively, in depth-first order.
		 * After returning from recursion, set {@code node}'s time to the lowest reachable one.
		 * Otherwise, if the time does not need to be adjusted, there is an SCC containing {@code node}
		 * and all other nodes above it in the stack.
		 */
		private void dfs(int node) {
			visit(node);
			boolean isComponentRoot = true;
			
			// Using an Iterator instead of a Stream, because nodes are visited recursively.
			// In particular, each iteration depends on state modifications from earlier iterations.
			Successors successors = graph.successors(node);
			int suc;
			while ((suc = successors.next()) >= 0) {
				if (!visited(suc))
					dfs(suc);
				int sucLow = nodeTimes[suc];
				if (nodeTimes[node] > sucLow) {
					// node can reach an earlier node -> it is not the root of the component
					nodeTimes[node] = sucLow;
					isComponentRoot = false;
				}
			}

			if (isComponentRoot)
				components++;
			
//			if (isComponentRoot) {
//				// node can reach all other nodes above it in the stack
//				// -> pop all these nodes from the stack and add them as a SCC to the result.
//				BitSet scc = new BitSet();
//				int n;
//				do {
//					n = pop();
//					scc.set(n);
//					nodeTimes[n] = Integer.MAX_VALUE;
//				} while (n != node);
//				components.add(scc);
//			}
		}
	}

}
