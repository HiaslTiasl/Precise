package it.unibz.precise.model;

import java.util.Map;

/**
 * Exception for an invalid location for wildcards used on intermediary levels in the hierarchy.
 * 
 * @author MatthiasP
 *
 */
public class MissingIntermediateEntryException extends InvalidLocationException {
	
	private static final long serialVersionUID = 1L;

	public MissingIntermediateEntryException(Task task, Map<String, PatternEntry> pattern, Attribute attr) {
		this(task, pattern, attr.getName());
	}

	public MissingIntermediateEntryException(Task task, Map<String, PatternEntry> pattern, String attrName) {
		super(task, pattern, String.format(
			"Attribute '%s' has a value, but other attributes higher in the hierarchy do not", attrName
		));
	}

}
