package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Task.class)
@JsonIdentityReference(alwaysAsId=false)
public class Task extends BaseEntity {

	@ManyToOne
	private TaskType taskType;
	@ManyToMany
	@JoinTable(
		joinColumns=@JoinColumn(name="task", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="constructionUnit", referencedColumnName="id")
	)
	@OrderColumn(nullable=false)
	private List<ConstructionUnit> constructionUnits;
	private Scope orderScope;
	
	@ManyToOne
	@JsonBackReference
	private Flow flow;
	
	public Task() {
	}
	
	public Task(TaskType taskType, List<ConstructionUnit> constructionUnits, Scope orderScope) {
		this.taskType = taskType;
		this.constructionUnits = constructionUnits;
		this.orderScope = orderScope;
	}

	public TaskType getTaskType() {
		return taskType;
	}
	
	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public Scope getOrderScope() {
		return orderScope;
	}
	
	public void setOrderScope(Scope orderScope) {
		this.orderScope = orderScope;
	}

	public List<ConstructionUnit> getConstructionUnits() {
		return constructionUnits;
	}
	
	public void setConstructionUnits(List<ConstructionUnit> constructionUnits) {
		this.constructionUnits = updateList(this.constructionUnits, constructionUnits);
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

}
