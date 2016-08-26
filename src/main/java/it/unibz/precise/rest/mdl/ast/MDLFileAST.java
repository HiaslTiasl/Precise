package it.unibz.precise.rest.mdl.ast;

import java.util.List;
import java.util.function.Function;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.util.Util;

@JsonPropertyOrder({"model", "attributes", "phases", "taskTypes", "tasks", "dependencies"})
@Validated
public class MDLFileAST {

	@JsonIgnoreProperties({"id", "name", "attributes", "phases", "taskTypes", "tasks", "dependencies"})
	private Model model;
	
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	private List<MDLTaskTypeAST> taskTypes;
	private List<MDLTaskAST> tasks;
	private List<MDLDependencyAST> dependencies;
	
	private transient Function<Attribute, MDLAttributeAST> attributeTranslator;
	private transient Function<Phase, MDLPhaseAST> phaseTranslator;
	private transient Function<TaskType, MDLTaskTypeAST> taskTypeTranslator;
	private transient Function<Task, MDLTaskAST> taskTranslator;
	private transient Function<Dependency, MDLDependencyAST> dependencyTranslator;

	public MDLFileAST() {
	}
	
	public MDLFileAST(Model model) {
		this.model = model;
		attributes = Util.mapToList(model.getAttributes(), this::translate);
		phases = Util.mapToList(model.getPhases(), this::translate);
		taskTypes = Util.mapToList(model.getTaskTypes(), this::translate);
		tasks = Util.mapToList(model.getTasks(), this::translate);
		dependencies = Util.mapToList(model.getDependencies(), this::translate);
	}
	
	public @Valid Model toModel(String name) {
		model.setName(name);
		attributes.stream()
			.map(MDLAttributeAST::toAttribute)
			.forEach(model::addAttribute);
		phases.stream()
			.map(MDLPhaseAST::toPhase)
			.forEach(model::addPhase);
		taskTypes.stream()
			.map(MDLTaskTypeAST::toTaskType)
			.forEach(model::addTaskType);
		tasks.stream()
			.map(MDLTaskAST::toTask)
			.forEach(model::addTask);
		dependencies.stream()
			.map(MDLDependencyAST::toDependency)
			.forEach(model::addDependency);
		return model;
	}
	
	public List<MDLAttributeAST> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.attributes = attributes;
	}
	
	public List<MDLPhaseAST> getPhases() {
		return phases;
	}
	
	public void setPhases(List<MDLPhaseAST> phases) {
		this.phases = phases;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}

	public List<MDLTaskTypeAST> getTaskTypes() {
		return taskTypes;
	}
	
	public void setTaskTypes(List<MDLTaskTypeAST> taskTypes) {
		this.taskTypes = taskTypes;
	}
	
	public List<MDLTaskAST> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<MDLTaskAST> tasks) {
		this.tasks = tasks;
	}
	
	public List<MDLDependencyAST> getDependencies() {
		return dependencies;
	}
	
	public void setDependencies(List<MDLDependencyAST> dependencies) {
		this.dependencies = dependencies;
	}
	
	public MDLAttributeAST translate(Attribute attribute) {
		if (attributeTranslator == null)
			attributeTranslator = Util.memoize(a -> new MDLAttributeAST(this, a));
		return attributeTranslator.apply(attribute);
	}
	
	public MDLPhaseAST translate(Phase phase) {
		if (phaseTranslator == null)
			phaseTranslator = Util.memoize(p -> new MDLPhaseAST(this, p));
		return phaseTranslator.apply(phase);
	}
	
	public MDLTaskTypeAST translate(TaskType taskType) {
		if (taskTypeTranslator == null)
			taskTypeTranslator = Util.memoize(tt -> new MDLTaskTypeAST(this, tt));
		return taskTypeTranslator.apply(taskType);
	}
	
	public MDLTaskAST translate(Task task) {
		if (taskTranslator == null)
			taskTranslator = Util.memoize(t -> new MDLTaskAST(this, t));
		return taskTranslator.apply(task);
	}
	
	public MDLDependencyAST translate(Dependency dependency) {
		if (dependencyTranslator == null)
			dependencyTranslator = Util.memoize(d -> new MDLDependencyAST(this, d));
		return dependencyTranslator.apply(dependency);
	}
	
	public static MDLFileAST translate(Model model) {
		return model == null ? null : new MDLFileAST(model);
	}
	
}
