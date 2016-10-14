package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.ManyToMany;

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
	
	public void updateType(List<Attribute> allAttributes) {
		int attrCount = attributes == null ? 0 : attributes.size();
		int totalAttrCount = allAttributes == null ? 0 : allAttributes.size();
		if (attrCount == totalAttrCount)
			type = Type.UNIT;
		else if (attrCount == 0)
			type = Type.GLOBAL;
		else if (attrCount > 0 && attrCount < totalAttrCount)
			type = Type.ATTRIBUTES;
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
	
	public void update(List<Attribute> allAttributes) {
		switch (type) {
		case UNIT:
			setAttributes(allAttributes);
			break;
		case GLOBAL:
			setAttributes(null);
			break;
		case ATTRIBUTES:
			updateType(allAttributes);
			break;
		}
	}

}
