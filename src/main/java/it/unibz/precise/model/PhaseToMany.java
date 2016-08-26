package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.CollectionBidirection;

public class PhaseToMany {

	static final CollectionBidirection<Phase, AttributeHierarchyLevel, List<AttributeHierarchyLevel>> LEVELS =
		new CollectionBidirection<>(
			Phase::getAttributeHierarchyLevels,
			Phase::internalSetLevels,
			AttributeHierarchyLevel::getPhase,
			AttributeHierarchyLevel::internalSetPhase
		);

}
