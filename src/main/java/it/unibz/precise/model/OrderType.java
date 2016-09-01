package it.unibz.precise.model;

public enum OrderType {
	NONE(false), PARALLEL(false), ASCENDING(true), DESCENDING(true);
	
	private boolean requiresOrdered;
	
	OrderType(boolean requiresOrdered) {
		this.requiresOrdered = requiresOrdered;
	}
	
	public boolean isAssignableTo(Attribute attribute) {
		return !requiresOrdered || attribute.isOrdered();
	}
}
