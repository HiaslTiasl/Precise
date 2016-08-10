package it.unibz.precise.rest.mdl;

import java.util.Map;

import it.unibz.precise.rest.mdl.ast.MDLTaskAST;

public class InvalidLocationException extends MDLException {

	private static final long serialVersionUID = 1L;
	
	public InvalidLocationException(Map<String, String> locationMap, String msg) {
		super(String.format(
			"Invalid location: %s. %s", MDLTaskAST.toLocationMapString(locationMap), msg
		));
	}

}
