package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Attribute;

/**
 * Attribute without range.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="attributeSummary", types=Attribute.class)
public interface AttributeSummaryProjection {

	String getName();
	
	String getShortName();
	
	boolean isOrdered();
	
	boolean isPerPhase();
	
}
