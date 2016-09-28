package it.unibz.precise.rest.mdl.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Phase;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;

public class PhaseTranslator extends AbstractMDLTranslator<Phase, MDLPhaseAST> {
	
	public PhaseTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Phase phase, MDLPhaseAST mdlPhase) {
		List<AttributeHierarchyLevel> levelList = phase.getAttributeHierarchyLevels();
		mdlPhase.setName(phase.getName());
		mdlPhase.setDescription(phase.getDescription());
		mdlPhase.setColor(phase.getColor());
		mdlPhase.setAttributes(phase.getAttributeHierarchyLevels().stream()
			.map(AttributeHierarchyLevel::getAttribute)
			.map(context().attributes()::toMDL)
			.collect(Collectors.toList()));
		mdlPhase.setValueTree(levelList.isEmpty() ? null : createTree(levelList.get(0).getNodes()));
	}
	
	@Override
	public void updateEntity(MDLPhaseAST mdlPhase, Phase phase) {
		phase.setName(mdlPhase.getName());
		phase.setDescription(mdlPhase.getDescription());
		phase.setColor(mdlPhase.getColor());
		mdlPhase.getAttributes().stream()
			.map(context().attributes()::toEntity)
			.forEach(phase::addAttribute);
		walkTree(phase.getAttributeHierarchyLevels(), 0, mdlPhase.getValueTree(), null);
	}
	
	@Override
	public Phase createEntity() {
		return new Phase();
	}

	@Override
	public MDLPhaseAST createMDL() {
		return new MDLPhaseAST();
	}
	
	private Object createTree(Map<String, AttributeHierarchyNode> nodes) {
		Map<String, Object> tree = new LinkedHashMap<>();
		boolean hasSubTrees = false;
		for (AttributeHierarchyNode node : nodes.values()) {
			Object subTree = null;
			Map<String, AttributeHierarchyNode> children = node.getChildren();
			if (!children.isEmpty()) {
				hasSubTrees = true;
				if (node.isValuesMatchPositions())
					subTree = children.size();
				else
					subTree = createTree(children);
			}
			tree.put(node.getValue(), subTree);
		}
		return hasSubTrees ? tree : tree.keySet();
	}

	private void walkTree(List<AttributeHierarchyLevel> levels, int levelIndex, Object tree, AttributeHierarchyNode parent) {
		if (tree instanceof Integer)
			walkTreeLevel(levels, levelIndex, (int)tree, parent);
		else if (tree instanceof List)
			walkTreeLevel(levels, levelIndex, (List<?>)tree, parent);
		else if (tree instanceof Map)
			walkTreeLevel(levels, levelIndex, (Map<?,?>)tree, parent);
	}
	
	private void walkTreeLevel(List<AttributeHierarchyLevel> levels, int levelIndex, int totalNum, AttributeHierarchyNode parent) {
		AttributeHierarchyLevel level = levels.get(levelIndex);
		Attribute attr = level.getAttribute();
		parent.setValuesMatchPositions(true);
		for (int value = 1; value <= totalNum; value++)
			createNode(level, parent, attr.checkValue(value));
	}
	
	private void walkTreeLevel(List<AttributeHierarchyLevel> levels, int levelIndex, List<?> leafs, AttributeHierarchyNode parent) {
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
