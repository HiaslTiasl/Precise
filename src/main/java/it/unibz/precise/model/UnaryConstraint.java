package it.unibz.precise.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("1")
public class UnaryConstraint extends Constraint<UnaryKind> {
	
	@ManyToOne
	private Task task;

	public UnaryConstraint() {
	}

	public UnaryConstraint(UnaryKind kind, Scope scope, Task task) {
		super(kind, scope);
		this.task = task;
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
}
