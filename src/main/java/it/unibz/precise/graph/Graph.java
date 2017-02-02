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

}