package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
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
	
	public List<PatternEntry> getPattern() {
		return toPattern(this, level.getPhase());
	}
	
	public static List<PatternEntry> toPattern(AttributeHierarchyNode node, Phase phase) {
		List<PatternEntry> pattern = new ArrayList<>();
		if (node != null)
			node.addAncestorsTo(pattern);
		addWildcardsTo(pattern, phase.getAttributeHierarchyLevels());
		return pattern;
	}
	
	private void addAncestorsTo(List<PatternEntry> pattern) {
		if (parent != null)
			parent.addAncestorsTo(pattern);
		pattern.add(new PatternEntry(level.getAttribute().getName(), value));
	}
	
	private static List<PatternEntry> addWildcardsTo(List<PatternEntry> pattern, List<AttributeHierarchyLevel> levels) {
		if (pattern == null)
			pattern = new ArrayList<>();
		ListIterator<AttributeHierarchyLevel> it = levels.listIterator(pattern.size());
		int len = levels.size();
		for (int i = pattern.size(); i < len; i++)
			pattern.add(new PatternEntry(it.next().getAttribute().getName()));
		return pattern;
	}
	
}
