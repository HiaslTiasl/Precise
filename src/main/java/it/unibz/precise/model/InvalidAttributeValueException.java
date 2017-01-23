package it.unibz.precise.model;

/**
 * An exception for values that are not contained in the attribute's range.
 * 
 * @author MatthiasP
 *
 */
public class InvalidAttributeValueException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidAttributeValueException(Attribute attribute, String value) {
		this(attribute.getName(), value);
	}

	public InvalidAttributeValueException(String attrName, String value) {
		super(String.format(
			"The value '%s'' is not in the range of the attribute '%s'",
			value,
			attrName
		));
	}

}
