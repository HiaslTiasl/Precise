package it.unibz.precise.model.projection;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.PatternEntry;

@Projection(name="valueTree", types=AttributeHierarchyNode.class)
public interface AttributeValueTreeProjection {
	
	@Value("#{target.level.attribute.name}")
	String getAttributeName();
	
	String getValue();
	
	List<PatternEntry> getPattern();

	Map<String, AttributeValueTreeProjection> getChildren();
	
}
