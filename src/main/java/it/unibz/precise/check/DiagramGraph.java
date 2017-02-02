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
	
	private boolean directed;

	public DiagramGraph(Collection<Task> tasks) {
		this(tasks, true);
	}
	
	/**
	 * Create a graph with the given tasks as nodes.
	 * The successor function is determined by {@link Task#getOut()} and {@link Dependency#getTarget()}.
	 * Depending on whether {@code directed} is set, predecessors are also considered as successors.
	 */
	public DiagramGraph(Collection<Task> tasks, boolean directed) {
		this.tasks = tasks;
		this.directed = directed;
	}
	
	public Collection<Task> nodes() {
		return tasks;
	}
	
	/** Returns a stream of the predecessors of the given task. */
	private Stream<Task> predecessorsImpl(Task node) {
		return node.getIn().stream()
			.map(Dependency::getSource)
			.filter(Objects::nonNull);
	}
	
	/** Returns a stream of the successors of the given task. */
	private Stream<Task> successorsImpl(Task node) {
		return node.getOut().stream()
			.map(Dependency::getTarget)
			.filter(Objects::nonNull);
	}

	@Override
	public Stream<Task> successors(Task node) {
		Stream<Task> s = successorsImpl(node);
		return directed ? s : Stream.concat(s, predecessorsImpl(node));
	}
	
	/** Creates a directed graph with the given tasks as nodes. */
	public static DiagramGraph directed(Collection<Task> tasks) {
		return new DiagramGraph(tasks);
	}
	
	/** Creates a directed graph of the given model. */
	public static DiagramGraph directed(Model model) {
		return new DiagramGraph(model.getTasks());
	}
	
	/** Creates an undirected graph with the given tasks as nodes. */
	public static DiagramGraph undirected(Collection<Task> tasks) {
		return new DiagramGraph(tasks, false);
	}
	
	/** Creates an undirected graph of the given model. */
	public static DiagramGraph undirected(Model model) {
		return new DiagramGraph(model.getTasks(), false);
	}

}
