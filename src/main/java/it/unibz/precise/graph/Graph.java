package it.unibz.precise.graph;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A generic graph.
 * Defines a graph in terms of a set of nodes and a {@link Function} 
 * from nodes to its successors.
 * 
 * This avoids collecting all successors of all nodes into sets if not necessary.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes in the graph
 */
public class Graph<T> {
	
	private Collection<T> nodes;
	private Function<T, Stream<T>> getSuccessors;
	
	/** Construct a graph from a set of nodes and a successor function. */
	public Graph(Collection<T> nodes, Function<T, Stream<T>> getSuccessors) {
		this.nodes = nodes;
		this.getSuccessors = getSuccessors;
	}

	/** Returns the nodes. */
	public Collection<T> nodes() {
		return nodes;
	}

	/** Returns the successors of {@code node}. */
	public Stream<T> successors(T node) {
		return getSuccessors.apply(node);
	}
	
}
