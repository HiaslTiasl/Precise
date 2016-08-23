package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.OneToManyBidirection;

public class NodeToMany {

	static final OneToManyBidirection<AttributeHierarchyNode, AttributeHierarchyNode, List<AttributeHierarchyNode>> CHILDREN =
		new OneToManyBidirection<>(
			AttributeHierarchyNode::getChildren,
			AttributeHierarchyNode::internalSetChildren,
			AttributeHierarchyNode::getParent,
			AttributeHierarchyNode::internalSetParent
		);

}
