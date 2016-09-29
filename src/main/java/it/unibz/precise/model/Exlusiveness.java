package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Embeddable
public class Exlusiveness {
	
	public enum Type {
		NONE, GLOBAL, ATTRIBUTES;
	}
	
	private Type type;
	
	private List<Attribute> attributes;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
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
	
	@PostLoad
	@PreUpdate
	@PrePersist
	public void updateDependentFields() {
		updateAttributes();
	}

}
