package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Attribute;

@Projection(name="attribute", types=Attribute.class)
public interface AttributeProjection {

	String getName();
	
	boolean isOrdered();
	
	boolean isValuesMatchPositions();
	
}
