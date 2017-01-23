package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import it.unibz.precise.model.Scope;

/**
 * Represents a scope in an MDL file.
 * 
 * @author MatthiasP
 *
 */
public class MDLScopeAST {
	
	private Scope.Type type;
	
	private List<MDLAttributeAST> attributes;

	public Scope.Type getType() {
		return type;
	}

	public void setType(Scope.Type type) {
		this.type = type;
	}

	public List<MDLAttributeAST> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.attributes = attributes;
	}
	
}
