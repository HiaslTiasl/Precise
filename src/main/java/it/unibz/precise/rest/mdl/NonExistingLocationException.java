package it.unibz.precise.rest.mdl;

import java.util.Map;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;

public class NonExistingLocationException extends InvalidLocationException {

	private static final long serialVersionUID = 1L;

	public NonExistingLocationException(Map<String, String> locationMap,
		Map<String, String> parentMap, Attribute attr, String value)
	{
		this(locationMap, parentMap, attr.getName(), value);
	}

	public NonExistingLocationException(Map<String, String> locationMap,
		Map<String, String> parentMap, String attrName, String value)
	{
		super(locationMap, "There is no " + attrName + "=" + value + " in location " + MDLTaskAST.toLocationMapString(parentMap));
	}

}
