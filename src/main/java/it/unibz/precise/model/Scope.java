package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.ManyToMany;

@Embeddable
public class Scope {
	
	public enum Type {
		NONE, GLOBAL, ATTRIBUTES;
	}
	
	private Type type;
	
	@ManyToMany
	private List<Attribute> attributes;
	
	public Scope() {
		this(Type.NONE);
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
	
	public void updateType() {
		if (!isAttributesEmpty() && type != Type.ATTRIBUTES)
			type = Type.ATTRIBUTES;
	}
	
	public void onEmptyAttributes(Type type) {
		if (isAttributesEmpty())
			this.type = type;
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
