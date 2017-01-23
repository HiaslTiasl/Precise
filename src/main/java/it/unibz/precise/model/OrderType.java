package it.unibz.precise.model;

/**
 * Specifies a way in which an attribute contributes to the ordering constraint.
 * 
 * @author MatthiasP
 *
 */
public enum OrderType {
	NONE(false), PARALLEL(false), ASCENDING(true), DESCENDING(true);
	
	private boolean requiresOrdered;	// Must the range of attributes be ordered?
	
	OrderType(boolean requiresOrdered) {
		this.requiresOrdered = requiresOrdered;
	}
	
	/**
	 * Determines whether an this {@code OrderType} can be assigned to the given attribute.
	 * @return {@code false} if the attribute must have an ordered range but does not, {@code true} otherwise.
	 */
	public boolean isAssignableTo(Attribute attribute) {
		return !requiresOrdered || attribute.isOrdered();
	}
}
