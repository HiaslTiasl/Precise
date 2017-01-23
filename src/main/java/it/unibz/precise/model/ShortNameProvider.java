package it.unibz.precise.model;

/**
 * An object with a name that provides an alternative, shorter name that can be used
 * where space is limited. 
 * 
 * @author MatthiasP
 *
 */
public interface ShortNameProvider {
	
	/** The usual name of the object. */
	String getName();
	
	/** An alternative shorted name. */
	String getShortName();
	
}
