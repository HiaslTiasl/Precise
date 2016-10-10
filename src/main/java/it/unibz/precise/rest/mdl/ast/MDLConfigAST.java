package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Model;

@JsonPropertyOrder({"hoursPerDay", "attributes", "phases", "taskDefinitions"})
public class MDLConfigAST {
	
	public static final MDLConfigAST EMPTY_CONFIG = new MDLConfigAST();
	
	private int hoursPerDay = Model.DEFAULT_HOURS_PER_DAY;
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	private List<MDLTaskTypeAST> taskDefinitions;
	
	public int getHoursPerDay() {
		return hoursPerDay;
	}

	public void setHoursPerDay(int hoursPerDay) {
		this.hoursPerDay = hoursPerDay;
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

	public List<MDLTaskTypeAST> getTaskDefinitions() {
		return taskDefinitions;
	}

	public void setTaskDefinitions(List<MDLTaskTypeAST> taskDefinitions) {
		this.taskDefinitions = taskDefinitions;
	}
	
}
