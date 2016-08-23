package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.AttributeHierarchyNode;

@Projection(name="valueTree", types=AttributeHierarchyNode.class)
public interface AttributeValueTreeProjection {
	
	@Value("#{target.level.attribute.name}")
	String getAttributeName();
	
	String getValue();

	List<AttributeValueTreeProjection> getChildren();
	
}
