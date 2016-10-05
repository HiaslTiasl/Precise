package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Embeddable
public class Scope {
	
	private boolean global;
	
	private List<Attribute> attributes;

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void updateAttributes() {
		if (global)
			attributes = null;
	}
	
	@PostLoad
	@PreUpdate
	@PrePersist
	public void updateDependentFields() {
		updateAttributes();
	}

}
