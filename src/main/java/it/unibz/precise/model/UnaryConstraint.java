package it.unibz.precise.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@DiscriminatorValue("1")
public class UnaryConstraint extends Constraint<UnaryKind> {
	
	// Used to ensure that renaming the field does not break deserialization.
	public static final String TASK_FIELD_NAME = "task";
	
	@ManyToOne
	@JsonProperty(TASK_FIELD_NAME)
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
