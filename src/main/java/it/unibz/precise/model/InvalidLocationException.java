package it.unibz.precise.model;

import java.util.Map;

public class InvalidLocationException extends InvalidTaskException {

	private static final long serialVersionUID = 1L;
	
	public InvalidLocationException(Task task, Map<String, PatternEntry> pattern, String msg) {
		super(task, String.format(
			"Invalid location: %s. %s", PatternEntry.toKeyValueString(pattern), msg
		));
	}

}
