package it.unibz.precise.model;

import java.util.Map;

public class LocationHierarchyMismatchException extends InvalidLocationException {

	private static final long serialVersionUID = 1L;

	public LocationHierarchyMismatchException(Task task, Map<String, PatternEntry> pattern) {
		super(task, pattern, "The location has an invalid number of entries for the given hierarchy");
	}

}
