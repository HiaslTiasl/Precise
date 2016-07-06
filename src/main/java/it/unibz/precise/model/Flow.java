package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Flow.class)
@JsonIdentityReference(alwaysAsId=false)
public class Flow extends BaseEntity {
	
	private String name;
	@Lob
	private String description;

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
	private List<Constraint<? extends ConstraintKind>> constraints;
	
	public Flow() {
	}
	
	public Flow(List<Task> tasks, List<Constraint<? extends ConstraintKind>> constraints) {
		this.tasks = tasks;
		this.constraints = constraints;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<Task> tasks) {
		this.tasks = updateList(this.tasks, tasks);
	}

	public List<Constraint<? extends ConstraintKind>> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Constraint<? extends ConstraintKind>> constraints) {
		this.constraints = updateList(this.constraints, constraints);
	}
	
}
