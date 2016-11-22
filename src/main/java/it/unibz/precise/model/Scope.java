package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.ManyToMany;

import it.unibz.util.Util;

@Embeddable
public class Scope {
	
	public enum Type {
		UNIT, GLOBAL, ATTRIBUTES;
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
	
	public void updateType(List<Attribute> allAttributes, boolean strict) {
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
	
	public void update(List<Attribute> allowedAttributes) {
		update(allowedAttributes, true);
	}

	public void update(List<Attribute> allowedAttributes, boolean strict) {
		switch (type) {
		case UNIT:
			setAttributes(allowedAttributes);
			break;
		case GLOBAL:
			setAttributes(null);
			break;
		case ATTRIBUTES:
			updateType(allowedAttributes, strict);
			break;
		}
	}

	@Override
	public String toString() {
		return "Scope [type=" + type + ", attributes=" + attributes + "]";
	}
	
}
