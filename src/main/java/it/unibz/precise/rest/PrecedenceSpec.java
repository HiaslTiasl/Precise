package it.unibz.precise.rest;

import it.unibz.precise.model.Scope;

public class PrecedenceSpec {

	private ConstrainedTask source;
	private ConstrainedTask target;
	private Scope scope;
	
	public PrecedenceSpec() {
	}
	
	public PrecedenceSpec(ConstrainedTask source, ConstrainedTask target, Scope scope) {
		this.source = source;
		this.target = target;
		this.scope = scope;
	}

	public ConstrainedTask getSource() {
		return source;
	}
	
	public void setSource(ConstrainedTask source) {
		this.source = source;
	}
	
	public ConstrainedTask getTarget() {
		return target;
	}
	
	public void setTarget(ConstrainedTask target) {
		this.target = target;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
}
