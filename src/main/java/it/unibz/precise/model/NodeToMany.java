package it.unibz.precise.model;

import java.util.Map;

import it.unibz.util.MapBidirection;
import it.unibz.util.OneToManyBidirection;

/**
 * {@link OneToManyBidirection}s from {@link AttributeHierarchyNode} to other classes.
 * 
 * @author MatthiasP
 *
 */
public class NodeToMany {

	static final MapBidirection<String, AttributeHierarchyNode, AttributeHierarchyNode, Map<String, AttributeHierarchyNode>> CHILDREN =
		new MapBidirection<>(
			AttributeHierarchyNode::getChildren,
			AttributeHierarchyNode::internalSetChildren,
			AttributeHierarchyNode::getParent,
			AttributeHierarchyNode::internalSetParent,
			AttributeHierarchyNode::getValue
		);

}
