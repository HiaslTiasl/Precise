package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Phase;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.util.Util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PhaseTranslator extends AbstractMDLTranslator<Phase, MDLPhaseAST> {
	
	public PhaseTranslator(MDLContext context) {
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
		mdlPhase.setValueTree(phase.buildingTree());
	}
	
	@Override
	protected void updateEntityImpl(MDLPhaseAST mdlPhase, Phase phase) {
		phase.setName(mdlPhase.getName());
		phase.setDescription(mdlPhase.getDescription());
		phase.setColor(mdlPhase.getColor());
		phase.setAttributeHierarchyLevels(Util.mapToList(
			mdlPhase.getAttributes(),
			a -> new AttributeHierarchyLevel(context().attributes().toEntity(a))
		));
		walkTree(phase.getAttributeHierarchyLevels(), 0, mdlPhase.getValueTree(), null);
	}
	
	@Override
	public Phase createEntity(MDLPhaseAST mdl) {
		return new Phase();
	}

	@Override
	public MDLPhaseAST createMDL(Phase entity) {
		return new MDLPhaseAST();
	}
	
	private void walkTree(List<AttributeHierarchyLevel> levels, int levelIndex, Object tree, AttributeHierarchyNode parent) {
		if (tree instanceof Integer)
			walkTreeLevel(levels, levelIndex, (int)tree, parent);
		else if (tree instanceof Collection)
			walkTreeLevel(levels, levelIndex, (Collection<?>)tree, parent);
		else if (tree instanceof Map)
			walkTreeLevel(levels, levelIndex, (Map<?,?>)tree, parent);
	}
	
	private void walkTreeLevel(List<AttributeHierarchyLevel> levels, int levelIndex, int totalNum, AttributeHierarchyNode parent) {
		AttributeHierarchyLevel level = levels.get(levelIndex);
		Attribute attr = level.getAttribute();
		if (parent != null)
			parent.setValuesMatchPositions(true);
		for (int value = 1; value <= totalNum; value++)
			createNode(level, parent, attr.checkValue(value));
	}
	
	private void walkTreeLevel(List<AttributeHierarchyLevel> levels, int levelIndex, Collection<?> leafs, AttributeHierarchyNode parent) {
		AttributeHierarchyLevel level = levels.get(levelIndex);
		Attribute attr = level.getAttribute();
		for (Object value : leafs)
			createNode(level, parent, attr.checkValue(value));
	}
	
	private void walkTreeLevel(List<AttributeHierarchyLevel> levels, int levelIndex, Map<?, ?> tree, AttributeHierarchyNode parent) {
		AttributeHierarchyLevel level = levels.get(levelIndex);
		Attribute attr = level.getAttribute();
		for (Entry<?, ?> e : ((Map<?, ?>)tree).entrySet())
			walkTree(levels, levelIndex + 1, e.getValue(), createNode(level, parent, attr.checkValue(e.getKey())));
	}
	
	private AttributeHierarchyNode createNode(AttributeHierarchyLevel ahl, AttributeHierarchyNode parent, String value) {
		AttributeHierarchyNode node = new AttributeHierarchyNode(value);
		ahl.addNode(node);
		if (parent != null)
			parent.addChild(node);
		return node;
	}
	
}
