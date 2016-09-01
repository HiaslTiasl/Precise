package it.unibz.precise.model;

import java.util.Map;

public class MissingIntermediateEntryException extends InvalidLocationException {
	
	private static final long serialVersionUID = 1L;

	public MissingIntermediateEntryException(Map<String, PatternEntry> pattern, Attribute attr) {
		this(pattern, attr.getName());
	}

	public MissingIntermediateEntryException(Map<String, PatternEntry> pattern, String attrName) {
		super(pattern, String.format(
			"Attribute '%s' has no value, but other attributes lower in the hierarchy do", attrName
		));
	}

}
