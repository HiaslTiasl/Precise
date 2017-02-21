package it.unibz.precise.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A graph maintaining a {@link Set} of successor nodes for each node.
 * 
 * @author MatthiasP
 *
 * @param <T>
 */
public interface MaterializedGraph<T> extends Graph<T> {
	
	/** Returns the successors of {@code node}. */
	Set<T> successorSet(T node);
	
	default Stream<T> successors(T node) {
		return successorSet(node).stream();
	}
	
	default MaterializedGraph<T> materialize() {
		// Nothing to do
		return this;
	}
	
	/** Creates a {@code MaterializedGraph} of the given graph. */
	public static <T> MaterializedGraph<T> of(Graph<T> g) {
		Collection<T> nodes = g.nodes();
		return of(
			nodes,
			nodes.stream().collect(Collectors.toMap(
				Function.identity(),
				(T n) -> g.successors(n).collect(Collectors.toSet())
			)
		));
	}
	
	/** Creates a {@code MaterializedGraph} of the given map representing an adjacency list. */
	public static <T> MaterializedGraph<T> of(Collection<T> nodes, Map<T, Set<T>> adj) {
		return new MaterializedGraph<T>() {
			@Override
			public Collection<T> nodes() {
				return nodes;
			}
			@Override
			public Set<T> successorSet(T node) {
				Set<T> s = adj.get(node);
				return s != null ? s : Collections.emptySet();
			}
		};
	}
	
}
