package it.unibz.precise.model.projection;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Phase;

@Projection(name="expandedPhase", types=Phase.class)
public interface ExpandedPhaseProjection {
	
	long getId();
	
	String getName();
	
	Color getColor();
	
	@Value("#{target.attributeHierarchyLevels.![attribute]}")
	List<Attribute> getAttributes();
	
	@Value("#{target.buildingTree()}")
	Object getBuildingTree();

}
