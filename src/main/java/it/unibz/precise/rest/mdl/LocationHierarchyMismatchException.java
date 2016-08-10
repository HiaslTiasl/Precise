package it.unibz.precise.rest.mdl;

import java.util.Map;

public class LocationHierarchyMismatchException extends InvalidLocationException {

	private static final long serialVersionUID = 1L;

	public LocationHierarchyMismatchException(Map<String, String> locationMap) {
		super(locationMap, "The location has an invalid number of entries for the given hierarchy");
	}

	
	
}
