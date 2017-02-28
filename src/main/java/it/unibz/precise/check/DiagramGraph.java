package it.unibz.precise.check;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import it.unibz.precise.graph.Graph;
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
public class DiagramGraph implements Graph<Task> {
	
	private Collection<Task> tasks;
	
	/**
	 * Create a graph with the given tasks as nodes.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 */
	public DiagramGraph(Collection<Task> tasks) {
		this.tasks = tasks;
	}
	
	public Collection<Task> nodes() {
		return tasks;
	}
	
	/** Returns a stream of the successors of the given task. */
	@Override
	public Stream<Task> successors(Task node) {
		return node.getOut().stream()
			.map(d -> d.isPrecedence() ? d.getTarget() : null)
			.filter(Objects::nonNull);
	}

	/** Creates a directed graph with the given tasks as nodes. */
	public static DiagramGraph of(Collection<Task> tasks) {
		return new DiagramGraph(tasks);
	}
	
	/** Creates a directed graph of the given model. */
	public static DiagramGraph of(Model model) {
		return new DiagramGraph(model.getTasks());
	}

}
