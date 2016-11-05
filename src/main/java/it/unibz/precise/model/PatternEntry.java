package it.unibz.precise.model;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class PatternEntry {
	
	public static final String WILDCARD_VALUE = "*";

	@ManyToOne
	private String attributeName;
	private String value;
	private Collection<String> allowedValues;
	
	public PatternEntry() {
	}
	
	public PatternEntry(String attributeName) {
		this(attributeName, WILDCARD_VALUE);
	}
	
	public PatternEntry(String attributeName, String value) {
		this.attributeName = attributeName;
		this.value = value == null ? WILDCARD_VALUE : value;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Collection<String> getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(Collection<String> allowedValues) {
		this.allowedValues = allowedValues;
	}
	
	public void checkValue() {
		if (!allowedValues.contains(value))
			value = WILDCARD_VALUE;
	}
	
	public boolean hasValue() {
		return value != null && !PatternEntry.WILDCARD_VALUE.equals(value);
	}
	
	public static String toKeyValueString(Map<String, PatternEntry> pattern) {
		return pattern == null ? "[]" : pattern.values().stream()
			.map(e -> e.getAttributeName() + "=" + e.getValue())
			.collect(Collectors.joining(", ", "[", "]"));
	}
	
	public static String toValueString(Map<String, PatternEntry> pattern) {
		return pattern == null ? WILDCARD_VALUE
			: pattern.values().stream()
				.map(PatternEntry::getValue)
				.collect(Collectors.joining("-"));
	}
	
}
