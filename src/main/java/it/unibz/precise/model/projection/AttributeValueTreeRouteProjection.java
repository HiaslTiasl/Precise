package it.unibz.precise.model.projection;

import java.util.Map;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Phase;

@Projection(name="valueTree", types=Phase.class)
public interface AttributeValueTreeRouteProjection {
	
	Map<String, AttributeValueTreeProjection> getValueTree();
	
}
