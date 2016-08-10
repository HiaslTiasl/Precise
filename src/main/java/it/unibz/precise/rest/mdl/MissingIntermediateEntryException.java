package it.unibz.precise.rest.mdl;

import java.util.Map;

import it.unibz.precise.model.Attribute;

public class MissingIntermediateEntryException extends InvalidLocationException {
	
	private static final long serialVersionUID = 1L;

	public MissingIntermediateEntryException(Map<String, String> locationMap, Attribute attr) {
		this(locationMap, attr.getName());
	}

	public MissingIntermediateEntryException(Map<String, String> locationMap, String attrName) {
		super(locationMap, String.format(
			"Attribute '%s' has no value, but other attributes lower in the hierarchy do", attrName
		));
	}

}
