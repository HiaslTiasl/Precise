package it.unibz.precise.model;

import java.util.List;
import java.util.Map;

import it.unibz.util.Util;

public class PatternEntry {
	
	public static final String WILDCARD_VALUE = "*";

	private String attribute;
	private String value;
	
	public PatternEntry(String attribute) {
		this(attribute, WILDCARD_VALUE);
	}
	
	public PatternEntry(String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
	}

	public String getAttribute() {
		return attribute;
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public static Map<String, String> toMap(List<PatternEntry> pattern) {
		return Util.mapToMap(pattern, PatternEntry::getAttribute, PatternEntry::getValue);
	}
	
}
