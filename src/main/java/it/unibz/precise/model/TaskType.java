package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"}),
	@UniqueConstraint(columnNames={"model_id", "shortName"})
})
public class TaskType extends BaseEntity {
	
	@Column(nullable=false)
	private String name;
	@Column(nullable=false)
	private String shortName;
	private String description;
	private String unitOfMeasure;
	
	@ManyToOne
	private Craft craft;
	
	@OneToMany(mappedBy="type", cascade=CascadeType.REMOVE)
	private List<Task> tasks = new ArrayList<>();
	
	@ManyToOne
	private Phase phase;
	
	@ManyToOne
	private Model model;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public Craft getCraft() {
		return craft;
	}

	public void setCraft(Craft craft) {
		this.craft = craft;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		TaskTypeToMany.TASKS.setMany(this, tasks);
	}

	void internalSetTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	@Transient
	public Long getPhaseID() {
		return phase == null ? null : phase.getId();
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.TYPES.setOne(this, model);
	}
	
	void internalSetModel(Model model) {
		this.model = model;
	}

}
