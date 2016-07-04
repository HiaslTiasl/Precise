package it.unibz.precise.model;

public enum BinaryKind implements ConstraintKind {
	PRECEDENCE,
	ALTERNATE_PRECEDENCE,
	CHAIN_PRECEDENCE;
	
	public static int ARITY = 2;
	
	public int getArity() {
		return ARITY;
	}
}
