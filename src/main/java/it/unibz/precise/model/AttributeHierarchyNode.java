package it.unibz.precise.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class AttributeHierarchyNode extends BaseEntity {

	@ManyToOne
	private AttributeHierarchyLevel level;
	
	@ManyToOne
	private AttributeHierarchyNode parent;

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, orphanRemoval=true)
	@MapKey(name="value")
	private Map<String, AttributeHierarchyNode> children = new HashMap<>();
	
	@Column(nullable=false)
	private String value;
	
	/**
	 * Indicates whether the children values range from 1 to children.size().
	 * Typically the case for "unit"-like attributes. 
	 */
	private boolean valuesMatchPositions;
	
	public AttributeHierarchyNode() {
	}

	public AttributeHierarchyNode(String value) {
		this.value = value;
	}

	public AttributeHierarchyLevel getLevel() {
		return level;
	}

	public void setLevel(AttributeHierarchyLevel level) {
		LevelToMany.NODES.setOne(this, level);
	}
	
	void internalSetLevel(AttributeHierarchyLevel level) {
		this.level = level;
	}

	@Transient
	public Long getLevelID() {
		return level == null ? null : level.getId();
	}
	
	public AttributeHierarchyNode getParent() {
		return parent;
	}

	public void setParent(AttributeHierarchyNode parent) {
		NodeToMany.CHILDREN.setOne(this, parent);
	}
	
	void internalSetParent(AttributeHierarchyNode parent) {
		this.parent = parent;
	}

	@Transient
	public Long getParentID() {
		return parent == null ? null : parent.getId();
	}
	
	public Map<String, AttributeHierarchyNode> getChildren() {
		return children;
	}

	public void setChildren(Map<String, AttributeHierarchyNode> children) {
		NodeToMany.CHILDREN.setMany(this, children);
	}
	
	void internalSetChildren(Map<String, AttributeHierarchyNode> children) {
		this.children = children;
	}
	
	public void addChild(AttributeHierarchyNode node) {
		NodeToMany.CHILDREN.addOneOfMany(this, node);
	}
	
	void internalAddChild(AttributeHierarchyNode node) {
		children.put(node.getValue(), node);
	}
	
	public AttributeHierarchyNode findChildByValue(String value) {
		return children.get(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isValuesMatchPositions() {
		return valuesMatchPositions;
	}

	public void setValuesMatchPositions(boolean valuesMatchPositions) {
		this.valuesMatchPositions = valuesMatchPositions;
	}
	
	public Map<String, PatternEntry> getPattern() {
		return toPattern(this, level.getPhase().getAttributeHierarchyLevels());
	}
	
	public static Map<String, PatternEntry> toPattern(AttributeHierarchyNode node, List<AttributeHierarchyLevel> levels) {
		Map<String, PatternEntry> pattern = new LinkedHashMap<>();
		if (node != null)
			node.addAncestorsTo(pattern);
		addWildcardsTo(pattern, levels);
		return pattern;
	}
	
	private static void addToPattern(Map<String, PatternEntry> pattern, Attribute attribute, String value) {
		String attrName = attribute.getName();
		pattern.put(attrName, new PatternEntry(attrName, value));
	}
	
	private void addToPattern(Map<String, PatternEntry> pattern) {
		addToPattern(pattern, level.getAttribute(), value);
	}
	
	private void addAncestorsTo(Map<String, PatternEntry> pattern) {
		if (parent != null)
			parent.addAncestorsTo(pattern);
		addToPattern(pattern);
	}
	
	private static Map<String, PatternEntry> addWildcardsTo(Map<String, PatternEntry> pattern, List<AttributeHierarchyLevel> levels) {
		if (pattern == null)
			pattern = new LinkedHashMap<>();
		int len = levels.size();
		for (int i = pattern.size(); i < len; i++)
			addToPattern(pattern, levels.get(i).getAttribute(), PatternEntry.WILDCARD_VALUE);
		return pattern;
	}
	
}
