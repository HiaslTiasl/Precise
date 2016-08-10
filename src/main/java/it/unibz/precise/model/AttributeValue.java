package it.unibz.precise.model;

import javax.persistence.ManyToOne;

//@Entity
public class AttributeValue extends BaseEntity implements Ordered {

	@ManyToOne
	private Attribute attribute;
	
	private String value;
	
	private int position;

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
}
