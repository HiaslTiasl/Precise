package it.unibz.precise.graph;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Graph interface.
 * Represents successors as a {@link Stream} of nodes so they can be determined lazily.
 * 
 * @author MatthiasP
 *
 * @param <T>
 */
public interface Graph<T> {
	
	/** Returns the nodes. */
	Collection<T> nodes();

	/** Returns the successors of {@code node}. */
	Stream<T> successors(T node);
	
	/**
	 * Materializes this graph.
	 * Useful to speed up traversal of successors, in particular when this graph requires
	 * a significant amount of work for finding the successors of a node, and to speed up
	 * testing whether a given node {@code n} has a successor node {@code s}.
	 */
	default MaterializedGraph<T> materialize() {
		return MaterializedGraph.of(this);
	}

}