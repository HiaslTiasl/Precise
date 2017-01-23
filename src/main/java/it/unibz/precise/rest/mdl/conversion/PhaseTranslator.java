package it.unibz.precise.rest.mdl.conversion;

import java.util.stream.Collectors;

import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.Phase;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.util.Util;

/**
 * {@link MDLTranslator} for phases.
 * 
 * @author MatthiasP
 *
 */
public class PhaseTranslator extends AbstractMDLTranslator<Phase, MDLPhaseAST> {
	
	PhaseTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Phase phase, MDLPhaseAST mdlPhase) {
		mdlPhase.setName(phase.getName());
		mdlPhase.setDescription(phase.getDescription());
		mdlPhase.setColor(phase.getColor());
		mdlPhase.setAttributes(phase.getAttributeHierarchyLevels().stream()
			.map(AttributeHierarchyLevel::getAttribute)
			.map(context().attributes()::toMDL)
			.collect(Collectors.toList()));
		mdlPhase.setValueTree(phase.getBuildingTree());
	}
	
	@Override
	protected void updateEntityImpl(MDLPhaseAST mdlPhase, Phase phase) {
		phase.setName(mdlPhase.getName());
		phase.setDescription(mdlPhase.getDescription());
		phase.setColor(mdlPhase.getColor());
		// TODO check whether creating new levels instead of using context() is safe in all cases
		phase.setAttributeHierarchyLevels(Util.mapToList(
			mdlPhase.getAttributes(),
			a -> new AttributeHierarchyLevel(context().attributes().toEntity(a))		
		));
		phase.setBuildingTree(mdlPhase.getValueTree());
	}
	
	@Override
	public Phase createEntity() {
		return new Phase();
	}

	@Override
	public MDLPhaseAST createMDL() {
		return new MDLPhaseAST();
	}
	
	
	
}
