package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Model;
import it.unibz.util.Util;

@JsonPropertyOrder({"model", "attributes", "phases", "taskTypes", "tasks", "dependencies"})
@Validated
public class MDLFileAST {
	
	@JsonIgnoreProperties({"id", "name", "state", "attributes", "phases", "taskTypes", "tasks", "dependencies"})
	private Model model;

	@JsonIgnore
	private MDLConfigAST config;
	
	private List<MDLTaskAST> tasks;
	private List<MDLDependencyAST> dependencies;
	
	private MDLFileContext context = new MDLFileContext();

	public MDLFileAST() {
		config = new MDLConfigAST();
	}
	
	public MDLFileAST(Model model) {
		this.model = model;
		config = new MDLConfigAST(context, model);
		tasks = Util.mapToList(model.getTasks(), context::translate);
		dependencies = Util.mapToList(model.getDependencies(), context::translate);
	}
	
	public Model toModel(String name) {
		model.setName(name);
		config.applyTo(model);
		tasks.stream()
			.map(MDLTaskAST::toTask)
			.forEach(model::addTask);
		dependencies.stream()
			.map(MDLDependencyAST::toDependency)
			.forEach(model::addDependency);
		return model;
	}
	
	@JsonProperty("attributes")
	public List<MDLAttributeAST> getAttributes() {
		return config.getAttributes();
	}
	
	@JsonProperty("attributes")
	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.config.setAttributes(attributes);
	}
	
	@JsonProperty("phases")
	public List<MDLPhaseAST> getPhases() {
		return config.getPhases();
	}
	
	@JsonProperty("phases")
	public void setPhases(List<MDLPhaseAST> phases) {
		this.config.setPhases(phases);
	}
	
	@JsonProperty("taskTypes")
	public List<MDLTaskTypeAST> getTaskTypes() {
		return config.getTaskTypes();
	}
	
	@JsonProperty("taskTypes")
	public void setTaskTypes(List<MDLTaskTypeAST> taskTypes) {
		config.setTaskTypes(taskTypes);
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
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
	
	public static MDLFileAST translate(Model model) {
		return model == null ? null : new MDLFileAST(model);
	}
	
}
