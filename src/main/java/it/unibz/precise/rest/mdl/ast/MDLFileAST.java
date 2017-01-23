package it.unibz.precise.rest.mdl.ast;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents an MDL file.
 * 
 * @author MatthiasP
 *
 */
@JsonPropertyOrder({"model", "configuration", "diagram"})
@Validated
public class MDLFileAST {
	
	public static final MDLFileAST EMPTY_FILE;
	
	static {
		EMPTY_FILE = new MDLFileAST();
		EMPTY_FILE.setConfiguration(MDLConfigAST.EMPTY_CONFIG);
		EMPTY_FILE.setDiagram(MDLDiagramAST.EMPTY_DIAGRAM);
	}
	
	private MDLModelAST model;
	
	private MDLConfigAST configuration;
	private MDLDiagramAST diagram;
	
	public MDLModelAST getModel() {
		return model;
	}
	
	public void setModel(MDLModelAST model) {
		this.model = model;
	}
	
	public MDLConfigAST getConfiguration() {
		return configuration;
	}

	public void setConfiguration(MDLConfigAST configuration) {
		this.configuration = configuration;
	}

	public MDLDiagramAST getDiagram() {
		return diagram;
	}
	
	public void setDiagram(MDLDiagramAST diagram) {
		this.diagram = diagram;
	}
	
}
