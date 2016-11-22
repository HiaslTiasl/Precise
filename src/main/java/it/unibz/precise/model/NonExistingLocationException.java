package it.unibz.precise.model;

import java.util.Map;

public class NonExistingLocationException extends InvalidLocationException {

	private static final long serialVersionUID = 1L;

	public NonExistingLocationException(Task task, Map<String, PatternEntry> pattern,
		Map<String, PatternEntry> parentPattern, Attribute attr, String value)
	{
		this(task, pattern, parentPattern, attr.getName(), value);
	}

	public NonExistingLocationException(Task task, Map<String, PatternEntry> pattern,
		Map<String, PatternEntry> parentPattern, String attrName, String value)
	{
		super(
			task, pattern,
			"There is no " + attrName + "=" + value + " in location " + PatternEntry.toKeyValueString(parentPattern)
		);
	}

}
