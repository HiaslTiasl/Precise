package it.unibz.precise.check;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.graph.MaterializedGraph;
import it.unibz.precise.graph.Successors;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

/**
 * A graph description of a process model diagram.
 * Nodes correspond to task boxes, arrows correspond to dependencies.
 * 
 * @author MatthiasP
 *
 */
public class DiagramGraph {
	
	private DiagramGraph() { }

	/**
	 * Creates a directed graph with the given tasks as nodes.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public static MaterializedGraph of(List<Task> tasks) {
		BitSet[] adj = new BitSet[tasks.size()];
		HashMap<Task, Integer> indexOf = new HashMap<>();
		int index = 0;
		for (Task t : tasks) {
			indexOf.put(t, index);
			adj[index++] = new BitSet();
		}
		tasks.stream()
			.map(Task::getOut)
			.flatMap(List::stream)
			.forEach(d -> adj[indexOf.get(d.getSource())].set(indexOf.get(d.getTarget())));
		return MaterializedGraph.of(adj);
	}
	
	/**
	 * Creates a directed graph of the given model.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public static MaterializedGraph of(Model model) {
		return of(model.getTasks());
	}

}
