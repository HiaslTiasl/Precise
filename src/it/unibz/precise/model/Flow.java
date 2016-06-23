package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=Flow.class)
@JsonIdentityReference(alwaysAsId=false)
public class Flow implements Identifiable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	
	@ManyToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinTable(
		joinColumns=@JoinColumn(name="flow", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="task", referencedColumnName="id")
	)
	private List<Task> tasks;
	
	@ManyToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
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
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}

	public List<FlowConstraint> getConstraints() {
		return constraints;
	}
	
}
