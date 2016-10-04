package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"model", "attributes", "phases", "taskTypes", "tasks", "dependencies"})
@Validated
public class MDLFileAST {
	
	private MDLModelAST model;
	
	@JsonIgnore
	private MDLConfigAST config;
	
	private List<MDLTaskAST> tasks;
	private List<MDLDependencyAST> dependencies;
	
	public MDLFileAST() {
		config = new MDLConfigAST();
	}
	
	@JsonIgnore
	public MDLConfigAST getConfig() {
		return config;
	}

	@JsonIgnore
	public void setConfig(MDLConfigAST config) {
		this.config = config;
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
	
	@JsonProperty("taskDefinitions")
	public List<MDLTaskTypeAST> getTaskDefinitions() {
		return config.getTaskDefinitions();
	}
	
	@JsonProperty("taskDefinitions")
	public void setTaskDefinitions(List<MDLTaskTypeAST> taskDefinitions) {
		config.setTaskDefinitions(taskDefinitions);
	}
	
	public MDLModelAST getModel() {
		return model;
	}
	
	public void setModel(MDLModelAST model) {
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
	
}
