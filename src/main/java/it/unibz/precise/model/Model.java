package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(name="UC_MODEL_NAME", columnNames="name"))
@JsonIgnoreProperties(value={"state"}, allowGetters=true)
public class Model extends BaseEntity {
	
	/**
	 * The state of the model.
	 * 
	 * There are 3 possible states depending on which among configuration and diagram is empty
	 * 
	 * state       | config    | diagram 
	 * ------------|-----------|----------
	 * EMPTY       |     empty |     empty
	 * CONFIGURING | not empty |     empty
	 * MODELLING   | not empty | not empty
	 * 
	 * Each state contains information about each part (config and diagram) via a PartInfo object.
	 */
	public static enum State {
		
		EMPTY(true, true),
		CONFIGURING(false, true),
		MODELLING(false, false);
		
		private PartInfo configInfo;
		private PartInfo diagramInfo;
		
		private State(boolean emptyConfig, boolean emptyDiagram) {
			this.configInfo  = new PartInfo(emptyConfig, emptyDiagram);
			this.diagramInfo = new PartInfo(emptyDiagram, !emptyConfig);
		}

		public PartInfo getConfigInfo() {
			return configInfo;
		}

		public PartInfo getDiagramInfo() {
			return diagramInfo;
		}
		
	}
	
	/**
	 * Encapsulates information about a model part, i.e. configuration or diagram.
	 */
	public static class PartInfo {
		
		private boolean empty;
		private boolean editable;
		
		public PartInfo(boolean empty, boolean editable) {
			this.empty = empty;
			this.editable = editable;
		}
		
		public boolean isEmpty() {
			return empty;
		}
		
		public boolean isEditable() {
			return editable;
		}
		
	}
	
	@Column(nullable=false)
	@NotNull(message="{model.name.notempty}")
	@Size(min=1, message="{model.name.notempty}")
	@Pattern(regexp="^[\\.\\w\\-_ ]*$", message="{model.name.pattern}")
	private String name;
	
	private String description;
	
	// Building configuration
	//---------------------------------------------------
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Attribute> attributes = new ArrayList<>();
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Phase> phases = new ArrayList<>();

	// Task configuration
	//---------------------------------------------------

	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<TaskType> taskTypes = new ArrayList<>();
	
	// Diagram
	//---------------------------------------------------

	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Task> tasks = new ArrayList<>();
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Dependency> dependencies = new ArrayList<>();

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
	
	private boolean isDiagramEmpty() {
		return tasks.isEmpty() && dependencies.isEmpty();
	}
	
	private boolean isConfigEmpty() {
		return phases.isEmpty() && attributes.isEmpty() && taskTypes.isEmpty();
	}
	
	public State getState() {
		return isConfigEmpty() ? State.EMPTY
			: isDiagramEmpty() ? State.CONFIGURING
			: State.MODELLING;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		ModelToMany.ATTRIBUTES.setMany(this, attributes);
	}
	
	void internalSetAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(Attribute attribute) {
		ModelToMany.ATTRIBUTES.addOneOfMany(this, attribute);
	}

	public List<Phase> getPhases() {
		return phases;
	}

	public void setPhases(List<Phase> phases) {
		ModelToMany.PHASES.setMany(this, phases);
	}
	
	void internalSetPhases(List<Phase> phases) {
		this.phases = phases;
	}
	
	public void addPhase(Phase phase) {
		ModelToMany.PHASES.addOneOfMany(this, phase);
	}

	public List<TaskType> getTaskTypes() {
		return taskTypes;
	}

	public void setTaskTypes(List<TaskType> taskTypes) {
		ModelToMany.TYPES.setMany(this, taskTypes);
	}
	
	void internalSetTaskTypes(List<TaskType> taskTypes) {
		this.taskTypes = taskTypes;
	}
	
	public void addTaskType(TaskType taskType) {
		ModelToMany.TYPES.addOneOfMany(this, taskType);
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<Task> tasks) {
		ModelToMany.TASKS.setMany(this, tasks);
	}
	
	public void addTask(Task task) {
		ModelToMany.TASKS.addOneOfMany(this, task);
	}
	
	void internalSetTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		ModelToMany.DEPENDENCIES.setMany(this, dependencies);
	}
	
	void internalSetDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public void addDependency(Dependency dependency) {
		ModelToMany.DEPENDENCIES.addOneOfMany(this, dependency);
	}
	
}
