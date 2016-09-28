package it.unibz.precise.rest.mdl.ast;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import it.unibz.precise.model.Attribute;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLAttributeAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLAttributeAST {

	@JsonIgnore
	private Attribute attribute;
	
	private String name;
	private String shortName;
	private String description;
	private Object range;
	private boolean ordered;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getRange() {
		return range;
	}

	public void setRange(Object range) {
		this.range = range;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

}
