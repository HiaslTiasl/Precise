package it.unibz.precise.model;

import java.util.Map;

import it.unibz.util.MapBidirection;
import it.unibz.util.OneToManyBidirection;

/**
 * {@link OneToManyBidirection}s from {@link AttributeHierarchyLevel} to other classes.
 * 
 * @author MatthiasP
 *
 */
public class LevelToMany {

	static final MapBidirection<String, AttributeHierarchyLevel, AttributeHierarchyNode, Map<String, AttributeHierarchyNode>> NODES =
		new MapBidirection<>(
			AttributeHierarchyLevel::getNodes,
			AttributeHierarchyLevel::internalSetNodes,
			AttributeHierarchyNode::getLevel,
			AttributeHierarchyNode::internalSetLevel,
			AttributeHierarchyNode::getValue
		);

}
