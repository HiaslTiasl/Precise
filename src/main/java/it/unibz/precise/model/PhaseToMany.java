package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.OneToManyBidirection;

public class PhaseToMany {

	static final OneToManyBidirection<Phase, AttributeHierarchyLevel, List<AttributeHierarchyLevel>> LEVELS =
	new OneToManyBidirection<>(
		Phase::getAttributeHierarchyLevels,
		Phase::internalSetLevels,
		AttributeHierarchyLevel::getPhase,
		AttributeHierarchyLevel::internalSetPhase
	);

}
