package it.unibz.precise.graph;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class Graph<T> {
	
	private Collection<T> nodes;
	private Function<T, Stream<T>> getSuccessors;
	
	public Graph(Collection<T> nodes, Function<T, Stream<T>> getSuccessors) {
		this.nodes = nodes;
		this.getSuccessors = getSuccessors;
	}

	public Collection<T> nodes() {
		return nodes;
	}

	public Stream<T> successors(T node) {
		return getSuccessors.apply(node);
	}
	
}
