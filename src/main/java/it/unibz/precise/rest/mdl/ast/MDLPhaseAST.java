package it.unibz.precise.rest.mdl.ast;

import java.awt.Color;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

/**
 * Represents a phase in an MDL file.
 * 
 * @author MatthiasP
 *
 */
@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLPhaseAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLPhaseAST {
	
	private String name;
	private String description;
	private Color color;
	private List<MDLAttributeAST> attributes;
	private Object valueTree;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public List<MDLAttributeAST> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.attributes = attributes;
	}

	public Object getValueTree() {
		return valueTree;
	}

	public void setValueTree(Object valueTree) {
		this.valueTree = valueTree;
	}

}
