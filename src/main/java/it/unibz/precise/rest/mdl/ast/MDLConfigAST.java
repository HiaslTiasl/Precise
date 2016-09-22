package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Model;
import it.unibz.util.Util;

@JsonPropertyOrder({"attributes", "phases"})
public class MDLConfigAST {
	
	private static final MDLConfigAST EMPTY_CONFIG = new MDLConfigAST();
	
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	private List<MDLTaskTypeAST> taskTypes;
	
	public MDLConfigAST() {
	}
	
	public MDLConfigAST(MDLFileContext context, Model model) {
		attributes = Util.mapToList(model.getAttributes(), context::translate);
		phases = Util.mapToList(model.getPhases(), context::translate);
		taskTypes = Util.mapToList(model.getTaskTypes(), context::translate);
	}
	
	public void applyTo(Model model) {
		model.setAttributes(Util.mapToList(attributes, MDLAttributeAST::toAttribute));
		model.setPhases(Util.mapToList(phases, MDLPhaseAST::toPhase));
		model.setTaskTypes(Util.mapToList(taskTypes, MDLTaskTypeAST::toTaskType));
	}
	
	public static void clearConfigOf(Model model) {
		EMPTY_CONFIG.applyTo(model);
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

	public List<MDLTaskTypeAST> getTaskTypes() {
		return taskTypes;
	}

	public void setTaskTypes(List<MDLTaskTypeAST> taskTypes) {
		this.taskTypes = taskTypes;
	}
	
}
