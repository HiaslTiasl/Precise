package it.unibz.precise.rest.mdl.ast;

import it.unibz.precise.model.Attribute;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLAttributeAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLAttributeAST {

	@JsonIgnore
	private Attribute attribute;
	
	private String name;
	private String description;
	private Object range;
	private boolean ordered;
	
	public MDLAttributeAST() {
	}

	public MDLAttributeAST(MDLFileContext context, Attribute attribute) {
		this.attribute = attribute;
		List<String> range = attribute.getRange();
		name = attribute.getName();
		description = attribute.getDescription();
		this.range = attribute.isValuesMatchPositions() ? range.size() : range;
		ordered = attribute.isOrdered();
	}
	
	public Attribute toAttribute() {
		if (attribute == null) {
			attribute = new Attribute();
			attribute.setName(name);
			attribute.setDescription(description);
			attribute.setRange(resolveRange(attribute, range));
			attribute.setOrdered(ordered);
		}
		return attribute;
	}
	
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

	private static List<String> resolveRange(Attribute attribute, Object range) {
		Stream<String> values;
		if (range instanceof Integer) {
			values = IntStream.rangeClosed(1, (int)range).mapToObj(String::valueOf);
			attribute.setValuesMatchPositions(true);
		}
		else if (range instanceof List)
			values = ((List<?>)range).stream().map(String::valueOf);
		else
			throw new IllegalArgumentException("Range of attribute '" + attribute.getName() + "'; expected list or integer");
		return values.collect(Collectors.toList());
	}
	
}
