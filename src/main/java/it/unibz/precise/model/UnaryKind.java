package it.unibz.precise.model;

public enum UnaryKind implements ConstraintKind {
	EXCLUSIVE_EXISTENCE;
	
	public static int ARITY = 1;
	
	public int getArity() {
		return ARITY;
	}
}
