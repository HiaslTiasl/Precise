package it.unibz.precise.model;

import java.util.Map;

public class InvalidLocationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidLocationException(Map<String, PatternEntry> pattern, String msg) {
		super(String.format(
			"Invalid location: %s. %s", PatternEntry.toKeyValueString(pattern), msg
		));
	}

}
