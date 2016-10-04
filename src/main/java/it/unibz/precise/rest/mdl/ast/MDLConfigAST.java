package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"attributes", "phases", "taskDefinitions"})
public class MDLConfigAST {
	
	public static final MDLConfigAST EMPTY_CONFIG = new MDLConfigAST();
	
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	private List<MDLTaskTypeAST> taskDefinitions;
	
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

	public List<MDLTaskTypeAST> getTaskDefinitions() {
		return taskDefinitions;
	}

	public void setTaskDefinitions(List<MDLTaskTypeAST> taskDefinitions) {
		this.taskDefinitions = taskDefinitions;
	}
	
}
