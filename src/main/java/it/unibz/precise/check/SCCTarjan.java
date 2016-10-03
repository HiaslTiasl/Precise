package it.unibz.precise.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * https://github.com/indy256/codelibrary/blob/master/java/src/SCCTarjan.java
 */
// optimized version of https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
public class SCCTarjan implements SCCFinder {

	private List<List<Integer>> adj;
	private boolean[] visited;
	private Stack<Integer> stack;
	private int time;
	private int[] lowlink;
	private List<List<Integer>> components;

	public List<List<Integer>> findSCCs(List<List<Integer>> adj) {
		int n = adj.size();
		this.adj = adj;
		visited = new boolean[n];
		stack = new Stack<>();
		time = 0;
		lowlink = new int[n];
		components = new ArrayList<>();

		for (int u = 0; u < n; u++)
			if (!visited[u])
				dfs(u);

		return components;
	}

	private void dfs(int u) {
		lowlink[u] = time++;
		visited[u] = true;
		stack.add(u);
		boolean isComponentRoot = true;

		for (int v : adj.get(u)) {
			if (!visited[v])
				dfs(v);
			if (lowlink[u] > lowlink[v]) {
				lowlink[u] = lowlink[v];
				isComponentRoot = false;
			}
		}

		if (isComponentRoot) {
			List<Integer> component = new ArrayList<>();
			int x;
			do {
				x = stack.pop();
				component.add(x);
				lowlink[x] = Integer.MAX_VALUE;
			} while (x != u);
			components.add(component);
		}
	}

}
