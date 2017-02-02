package it.unibz.precise.graph.disj;

import java.util.Set;

/**
 * Represents a disjunctive edge between two sets of nodes,
 * indicating that all nodes of one set must be executed
 * before all nodes of the other set.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes
 */
public class DisjunctiveEdge<T> {
	
	private Set<T> left;
	private Set<T> right;

	public DisjunctiveEdge(Set<T> left, Set<T> right) {
		this.left = left;
		this.right = right;
	}

	public Set<T> getLeft() {
		return left;
	}

	public Set<T> getRight() {
		return right;
	}
	
	/** Indicates whether the two given nodes are excluded in the specified direction. */
	public boolean excludes(T l, T r) {
		return left.contains(l) && right.contains(r);
	}
	
	@Override
	public String toString() {
		return left + " --- " + right;
	}

}
