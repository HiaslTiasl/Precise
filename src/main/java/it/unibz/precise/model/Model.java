package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Model.class)
@JsonIdentityReference(alwaysAsId=false)
public class Model extends BaseEntity {
	
	private String name;
	@Lob
	private String description;

	@OneToMany(mappedBy="model", cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private List<TaskType> taskTypes;
	
	@OneToMany(mappedBy="model", cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private List<ConstructionUnit> constructionUnits;
	
	@OneToMany(mappedBy="model", cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private List<Task> tasks;
	
	@OneToMany(mappedBy="model", cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private List<Constraint<? extends ConstraintKind>> constraints;
	
	public Model() {
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

	public List<TaskType> getTaskTypes() {
		return taskTypes;
	}

	public void setTaskTypes(List<TaskType> taskTypes) {
		this.taskTypes = updateList(this.taskTypes, taskTypes);
		updateModelComponents(this.taskTypes);
	}

	public List<ConstructionUnit> getConstructionUnits() {
		return constructionUnits;
	}

	public void setConstructionUnits(List<ConstructionUnit> constructionUnits) {
		this.constructionUnits = constructionUnits;
		updateModelComponents(this.constructionUnits);
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = updateList(this.tasks, tasks);
		updateModelComponents(this.tasks);
	}

	public List<Constraint<? extends ConstraintKind>> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Constraint<? extends ConstraintKind>> constraints) {
		this.constraints = updateList(this.constraints, constraints);
		updateModelComponents(this.constraints);
	}
	
	private <T extends ModelComponent> List<T> updateModelComponents(List<T> list) {
		list.stream().filter(e -> e.getModel() != this).forEach(e -> e.setModel(this));
		return list;
	}
	
}
