package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Task.class)
@JsonIdentityReference(alwaysAsId=false)
public class Task extends BaseEntity implements ModelComponent {

	@ManyToOne
	private TaskType taskType;
	@OneToMany(mappedBy="task")
	@OrderBy("position")
	private List<TaskConstructionUnit> constructionUnits;
	private Scope orderScope;
	
	@ManyToOne
	private Model model;
	
	public Task() {
	}
	
	public Task(TaskType taskType, List<TaskConstructionUnit> constructionUnits, Scope orderScope) {
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

	public List<TaskConstructionUnit> getTaskConstructionUnits() {
		return constructionUnits;
	}
	
	public void setTaskConstructionUnits(List<TaskConstructionUnit> constructionUnits) {
		this.constructionUnits = updateList(this.constructionUnits, constructionUnits);
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
