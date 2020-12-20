package it.unibz.precise.graph.disj;

import java.util.BitSet;

/**
 * Represents a disjunctive edge between two sets of nodes,
 * indicating that all nodes of one set must be executed
 * before all nodes of the other set.
 * 
 * @author MatthiasP
 */
public class DisjunctiveEdge {
	
	private BitSet left;
	private BitSet right;

	public DisjunctiveEdge(BitSet left, BitSet right) {
		this.left = left;
		this.right = right;
	}
	
	public DisjunctiveEdge(int singleLeft, BitSet right) {
		this.left = new BitSet();
		this.left.set(singleLeft);
		this.right = right;
	}

	public BitSet getLeft() {
		return left;
	}

	public BitSet getRight() {
		return right;
	}
	
	/** 
	 * Returns either {@code left} or {@code right}, depending on which of the two contains {@code node},
	 * or null.
	 */
	public BitSet getSide(int node) {
		return left.get(node) ? left
			: right.get(node) ? right
			: null;
	}
	
	public BitSet getOther(BitSet side) {
		return side == left ? right
			: side == right ? left
			: null;
	}
	
	/** Indicates whether the two given nodes are excluded in the specified direction. */
	public boolean excludes(int l, int r) {
		return left.get(l) && right.get(r);
	}
	
	@Override
	public String toString() {
		return left + " --- " + right;
	}

}
