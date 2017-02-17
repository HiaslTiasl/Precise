package it.unibz.precise.graph;


/**
 * Represents the result of the resolve operation of a disjunctive edge.
 * 
 * @author MatthiasP
 *
 */
public enum ResolveType {
	PROBLEM,	// a problem was found (currently not used)
	NONE,		// no disjunctive edges were resolved
	OTHERS,		// other disjunctive edges were resolved	
	TARGET;		// the target edge was resolved
	
	/**
	 * Indicates whether this result can be immediately returned without further
	 * trying to resolve the target edge.
	 */
	public boolean isExitEarly() {
		switch (this) {
		case PROBLEM: 
		case TARGET:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Indicates whether the operation was successful, in the sense that at least
	 * some edges have been resolved.
	 */
	public boolean isSuccess() {
		switch (this) {
		case OTHERS:
		case TARGET:
			return true;
		default:
			return false;
		}
	}
	
	/** Merges the results of two attempts to resolve an edge. */
	public ResolveType merge(ResolveType that) {
		return this.isExitEarly() ? this
			: that.isExitEarly() ? that
			: this.isSuccess() ? this : that;
	}
	
}