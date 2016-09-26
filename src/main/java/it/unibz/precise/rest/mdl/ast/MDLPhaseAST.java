package it.unibz.precise.rest.mdl.ast;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Phase;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLPhaseAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLPhaseAST {
	
	@JsonIgnore
	private Phase phase;
	
	private String name;
	private String description;
	private Color color;
	private List<MDLAttributeAST> attributes;
	private Object valueTree;
	
	public MDLPhaseAST() {
	}
	
	public MDLPhaseAST(MDLFileContext context, Phase phase) {
		this.phase = phase;
		List<AttributeHierarchyLevel> levelList = phase.getAttributeHierarchyLevels();
		name = phase.getName();
		description = phase.getDescription();
		color = phase.getColor();
		attributes = phase.getAttributeHierarchyLevels().stream()
			.map(AttributeHierarchyLevel::getAttribute)
			.map(context::translate)
			.collect(Collectors.toList());
		valueTree = levelList.isEmpty() ? null : createTree(levelList.get(0).getNodes());
	}

	public Phase toPhase() {
		if (phase == null) {
			phase = new Phase();
			phase.setName(name);
			phase.setDescription(description);
			phase.setColor(color);
			attributes.stream()
				.map(MDLAttributeAST::toAttribute)
				.forEach(phase::addAttribute);
			walkTree(phase.nextLevel(null), valueTree, null);
		}
		return phase;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public List<MDLAttributeAST> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.attributes = attributes;
	}

	public Object getValueTree() {
		return valueTree;
	}

	public void setValueTree(Object valueTree) {
		this.valueTree = valueTree;
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

	private void walkTree(AttributeHierarchyLevel level, Object tree, AttributeHierarchyNode parent) {
		if (tree instanceof Integer)
			walkTreeLevel(level, (int)tree, parent);
		else if (tree instanceof List)
			walkTreeLevel(level, (List<?>)tree, parent);
		else if (tree instanceof Map)
			walkTreeLevel(level, (Map<?,?>)tree, parent);
	}
	
	private void walkTreeLevel(AttributeHierarchyLevel level, int totalNum, AttributeHierarchyNode parent) {
		Attribute attr = level.getAttribute();
		parent.setValuesMatchPositions(true);
		for (int value = 1; value <= totalNum; value++)
			createNode(level, parent, attr.checkValue(value));
	}
	
	private void walkTreeLevel(AttributeHierarchyLevel level, List<?> leafs, AttributeHierarchyNode parent) {
		Attribute attr = level.getAttribute();
		for (Object value : leafs)
			createNode(level, parent, attr.checkValue(value));
	}
	
	private void walkTreeLevel(AttributeHierarchyLevel level, Map<?, ?> tree, AttributeHierarchyNode parent) {
		Attribute attr = level.getAttribute();
		for (Entry<?, ?> e : ((Map<?, ?>)tree).entrySet())
			walkTree(phase.nextLevel(level), e.getValue(), createNode(level, parent, attr.checkValue(e.getKey())));
	}
	
	private AttributeHierarchyNode createNode(AttributeHierarchyLevel ahl, AttributeHierarchyNode parent, String value) {
		AttributeHierarchyNode node = new AttributeHierarchyNode(value);
		ahl.addNode(node);
		if (parent != null)
			parent.addChild(node);
		return node;
	}
	
}
