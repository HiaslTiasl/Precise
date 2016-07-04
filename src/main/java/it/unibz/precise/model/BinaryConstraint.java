package it.unibz.precise.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("2")
public class BinaryConstraint extends Constraint<BinaryKind> {

	@ManyToOne
	private Task source;
	@ManyToOne
	private Task target;
	
	public BinaryConstraint() {
	}

	public BinaryConstraint(BinaryKind kind, Scope scope, Task source, Task target) {
		super(kind, scope);
		this.source = source;
		this.target = target;
	}

	public Task getSource() {
		return source;
	}
	
	public void setSource(Task source) {
		this.source = source;
	}
	
	public Task getTarget() {
		return target;
	}
	
	public void setTarget(Task target) {
		this.target = target;
	}
	
	
}
