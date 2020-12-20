package it.unibz.precise.graph;

import java.util.BitSet;
import java.util.stream.Stream;

/**
 * Graph interface.
 * Represents successors as a {@link Stream} of nodes so they can be determined lazily.
 * 
 * @author MatthiasP
 */
public interface Graph {
	
	/** Returns the nodes. */
	int nodes();

	/** Returns a lazy representation of the successors of {@code node}. */
	Successors successors(int node);

	/**
	 * Returns a materialized representation of the successors of {@code node}.
	 * Useful to speed up traversal of successors, in particular when this graph requires
	 * a significant amount of work for finding the successors of a node, and to speed up
	 * testing whether a given node {@code n} has a successor node {@code s}.
	 */
	BitSet allSuccessors(int node);
	
	/**
	 * Mixin Interface for lazy implementations.
	 * Provides an implementation of {@link #allSuccessors(int)} based on {@link #successors(int)}.
	 */
	interface Lazy extends Graph {
		
		default BitSet allSuccessors(int node) {
			BitSet result = new BitSet(node);
			Successors succs = successors(node);
			for (int s = succs.next(); s >= 0; s = succs.next())
				result.set(s);
			return result;
		}
	}

	/**
	 * Mixin Interface for lazy implementations.
	 * Provides an implementation of {@link #allSuccessors(int)} based on {@link #successors(int)}.
	 */
	interface Eager extends Graph {

		default Successors successors(int node) {
			BitSet succs = allSuccessors(node);
			return new Successors() {
				int next = succs.nextSetBit(0);

				@Override
				public int next() {
					int result = next;
					if (next >= 0)
						next = succs.nextSetBit(next + 1);
					return result;
				}
			};
		}
	}
}