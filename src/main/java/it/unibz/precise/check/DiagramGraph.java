package it.unibz.precise.check;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

import java.util.Collection;
import java.util.Objects;

/**
 * A graph description of a process model diagram.
 * Nodes correspond to task boxes, arrows correspond to dependencies.
 * 
 * @author MatthiasP
 *
 */
public class DiagramGraph extends Graph<Task> {

	/**
	 * Create a graph with the given tasks as nodes.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public DiagramGraph(Collection<Task> tasks) {
		super(
			tasks,
			t -> t.getOut().stream()
				.map(Dependency::getTarget)
				.filter(Objects::nonNull)
		);
	}
	
	/**
	 * Create a graph with the given tasks as nodes.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public static DiagramGraph of(Collection<Task> tasks) {
		return new DiagramGraph(tasks);
	}
	
	/**
	 * Create a graph with the given model.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public static DiagramGraph of(Model model) {
		return new DiagramGraph(model.getTasks());
	}

}
