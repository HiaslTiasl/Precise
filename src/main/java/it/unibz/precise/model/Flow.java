package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Flow.class)
@JsonIdentityReference(alwaysAsId=false)
public class Flow extends BaseEntity {

	@ManyToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable(
		joinColumns=@JoinColumn(name="flow", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="task", referencedColumnName="id")
	)
	@JsonManagedReference
	private List<Task> tasks;
	
	@ManyToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable(
		joinColumns=@JoinColumn(name="flow", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="flowConstraint", referencedColumnName="id")
	)
	private List<FlowConstraint> constraints;
	
	public Flow() {
	}
	
	public Flow(List<Task> tasks, List<FlowConstraint> constraints) {
		this.tasks = tasks;
		this.constraints = constraints;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<Task> tasks) {
		this.tasks = updateList(this.tasks, tasks);
	}

	public List<FlowConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<FlowConstraint> constraints) {
		this.constraints = updateList(this.constraints, constraints);
	}
	
}
