package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"tasks", "dependencies"})
public class MDLDiagramAST {
	
	private List<MDLTaskAST> tasks;
	private List<MDLDependencyAST> dependencies;

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
