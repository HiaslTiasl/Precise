package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.OneToManyBidirection;

public class LevelToMany {

	static final OneToManyBidirection<AttributeHierarchyLevel, AttributeHierarchyNode, List<AttributeHierarchyNode>> NODES =
		new OneToManyBidirection<>(
			AttributeHierarchyLevel::getNodes,
			AttributeHierarchyLevel::internalSetNodes,
			AttributeHierarchyNode::getLevel,
			AttributeHierarchyNode::internalSetLevel
		);

}
