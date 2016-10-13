package it.unibz.precise.rest.mdl.ast;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"model", "config", "diagram"})
@Validated
public class MDLFileAST {
	
	private MDLModelAST model;
	
	private MDLConfigAST config;
	private MDLDiagramAST diagram;
	
	public MDLModelAST getModel() {
		return model;
	}
	
	public void setModel(MDLModelAST model) {
		this.model = model;
	}
	
	public MDLConfigAST getConfig() {
		return config;
	}

	public void setConfig(MDLConfigAST config) {
		this.config = config;
	}

	public MDLDiagramAST getDiagram() {
		return diagram;
	}
	
	public void setDiagram(MDLDiagramAST diagram) {
		this.diagram = diagram;
	}
	
}
