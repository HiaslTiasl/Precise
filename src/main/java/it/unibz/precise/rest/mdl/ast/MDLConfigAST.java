package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Model;
import it.unibz.util.Util;

@JsonPropertyOrder({"attributes", "phases"})
public class MDLConfigAST {
	
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	
	public MDLConfigAST() {
	}
	
	@JsonCreator
	public MDLConfigAST(
		@JsonProperty("attributes") List<MDLAttributeAST> attributes,
		@JsonProperty("phases") List<MDLPhaseAST> phases)
	{
		this.attributes = attributes;
		this.phases = phases;
	}

	public MDLConfigAST(MDLFileContext context, Model model) {
		attributes = Util.mapToList(model.getAttributes(), context::translate);
		phases = Util.mapToList(model.getPhases(), context::translate);
	}
	
	public void applyTo(Model model) {
		model.setAttributes(Util.mapToList(attributes, MDLAttributeAST::toAttribute));
		model.setPhases(Util.mapToList(phases, MDLPhaseAST::toPhase));
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
	
}
