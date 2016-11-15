package it.unibz.precise.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibz.precise.check.SCCFinder;

/**
 * https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java
 */
// optimized version of https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
public class SCCTarjan implements SCCFinder {


	public <T> List<List<T>> findSCCs(Graph<T> graph) {
		return new Run<T>(graph).dfs();
	}
	
	private static class Run<T> {
		private Graph<T> graph;
		private ArrayList<T> stack;
		private Map<T, Integer> lowlinks;
		private List<List<T>> components;
		private int time;
		
		private Run(Graph<T> graph) {
			this.graph = graph;
			int n = graph.nodes().size();
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
			for (T node : graph.nodes()) {
				if (!visited(node))
					dfs(node);
			}
			Collections.reverse(components);
			return components;
		}
		
		private void dfs(T node) {
			visit(node);
			boolean isComponentRoot = true;
			
			// Using an Iterator instead of a Stream, because nodes are visited recursively.
			// In particular, each iteration depends on state modifications from earlier iterations.
			Iterable<T> successors = graph.successors(node)::iterator;
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
				// Collections.reverse(component);
				components.add(component);
			}
		}
	}

}
