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
		this(Type.UNIT);
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
	
	public void updateType(int totalAttrCount, boolean unitAllowed) {
		int attrCount = attributes == null ? 0 : attributes.size();
		if (attrCount == totalAttrCount)
			type = Type.UNIT;
		else if (attrCount > 0 && attrCount < totalAttrCount)
			type = Type.ATTRIBUTES;
		else if (attrCount == 0 && type == Type.ATTRIBUTES)
			type = Type.GLOBAL;
		
		if (!unitAllowed && type == Type.UNIT)
			type = Type.ATTRIBUTES;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void updateAttributes() {
		if (type != Type.ATTRIBUTES)
			attributes = null;
	}
	
	public boolean isAttributesEmpty() {
		return attributes == null || attributes.isEmpty();
	}

}
