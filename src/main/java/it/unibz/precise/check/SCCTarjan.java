package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java
 */
// optimized version of https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
public class SCCTarjan implements SCCFinder {


	public <T> List<List<T>> findSCCs(Collection<T> nodes, Function<T, Stream<T>> adj) {
		return new Run<T>(nodes, adj).dfs();
	}
	
	private static class Run<T> {
		private Collection<T> nodes;
		private Function<T, Stream<T>> adj;
		private ArrayList<T> stack;
		private Map<T, Integer> lowlinks;
		private List<List<T>> components;
		private int time;
		
		private Run(Collection<T> nodes, Function<T, Stream<T>> adj) {
			int n = nodes.size();
			this.nodes = nodes;
			this.adj = adj;
			stack = new ArrayList<>(n);
			lowlinks = new HashMap<>(n);
			components = new ArrayList<>(n);
		}
		
		private boolean visited(T node) {
			return lowlinks.containsKey(node);
		}
		
		private void visit(T node) {
			lowlinks.put(node, time++);
			stack.add(node);
		}
		
		private T pop() {
			return stack.remove(stack.size() - 1);
		}
		
		private List<List<T>> dfs() {
			for (T node : nodes) {
				if (!visited(node))
					dfs(node);
			}
			return components;
		}
		
		private void dfs(T node) {
			visit(node);
			boolean isComponentRoot = true;
			
			// Using an Iterator instead of a Stream, because nodes are visited recursively.
			// In particular, each iteration depends on state modifications from earlier iterations.
			Iterable<T> successors = adj.apply(node)::iterator;
			for (T suc : successors) {
				if (!visited(suc))
					dfs(suc);
				int sucLow = lowlinks.get(suc);
				if (lowlinks.get(node) > sucLow) {
					lowlinks.put(node, sucLow);
					isComponentRoot = false;
				}
			}

			if (isComponentRoot) {
				List<T> component = new ArrayList<>();
				T n;
				do {
					n = pop();
					component.add(n);
					lowlinks.put(n, Integer.MAX_VALUE);
				} while (!n.equals(node));
				// We put nodes on the stack as we traverse the graph,
				// and add them to the component as we pop them from the stack.
				// By reversing the component afterwards, we get elements in topological order.
				Collections.reverse(component);
				components.add(component);
			}
		}
	}

}
