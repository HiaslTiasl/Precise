package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=Task.class)
@JsonIdentityReference(alwaysAsId=false)
public class Task implements Identifiable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	@ManyToOne
	private TaskType taskType;
	@ManyToMany
	@JoinTable(
		joinColumns=@JoinColumn(name="task", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="constructionUnit", referencedColumnName="id")
	)
	private List<ConstructionUnit> constructionUnits;
	private Scope orderScope;
	
	@ManyToOne
	private Flow flow;
	
	public Task() {
	}
	
	public Task(TaskType taskType, List<ConstructionUnit> constructionUnits, Scope orderScope) {
		this.taskType = taskType;
		this.constructionUnits = constructionUnits;
		this.orderScope = orderScope;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
		this.constructionUnits = constructionUnits;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

}
