package it.unibz.precise.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Embeddable;
import javax.persistence.ManyToMany;

import it.unibz.util.Util;

/**
 * Represents a scope in terms of attributes.
 * 
 * @author MatthiasP
 *
 */
@Embeddable
public class Scope {
	
	public enum Type {
		UNIT,				// all attributes of the phase
		GLOBAL,				// no attributes
		ATTRIBUTES;			// a proper, non-empty subset of the attributes in the phase
	}
	
	private Type type;
	
	@ManyToMany
	private List<Attribute> attributes;
	
	public Scope() {
	}

	public Scope(Type type) {
		this(type, null);
	}
	
	public Scope(Type type, List<Attribute> attributes) {
		this.type = type;
		this.attributes = attributes;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	/** Update {@code type} by comparing {@code attribute} to {@code allAttributes} */
	public void updateType(List<Attribute> allAttributes) {
		type = Util.size(attributes) == 0 ? Type.GLOBAL
			: Util.containSameElements(attributes, allAttributes) ? Type.UNIT
			: Type.ATTRIBUTES;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		if (this.attributes == null || attributes == null)
			this.attributes = attributes;
		else {
			// Hibernate does not allow to replace a persistent collection with a non-persistent one
			this.attributes.clear();
			this.attributes.addAll(attributes);
		}
	}

	/** Update the scope based on the given allowed attributes. */
	public void update(List<Attribute> allowedAttributes) {
		switch (type) {
		case UNIT:
			// we want to have as much attributes as possible
			setAttributes(allowedAttributes);
			break;
		case GLOBAL:
			// no attributes
			setAttributes(Collections.emptyList());
			break;
		case ATTRIBUTES:
			// change type if needed
			updateType(allowedAttributes);
			break;
		}
	}
	
	/**
	 * Projects the given {@link AttributeHierarchyNode} to this scope, returning a
	 * map of attributes to values for the attributes contained in this scope.
	 */
	public Map<Attribute, String> project(AttributeHierarchyNode node) {
		return node.ancestorStream()
			.filter(n -> attributes.contains(n.getLevel().getAttribute()))
			.collect(Collectors.toMap(n -> n.getLevel().getAttribute(), AttributeHierarchyNode::getValue));
	}

	@Override
	public String toString() {
		return "Scope [type=" + type + ", attributes=" + attributes + "]";
	}
	
}
