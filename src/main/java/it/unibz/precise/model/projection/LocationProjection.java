package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Location;

@Projection(name="location", types=Location.class)
public interface LocationProjection {
	
	int getId();

	int getLevel();
	
	AttributeHierarchyNode getNode();
	
}
