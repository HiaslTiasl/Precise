package it.unibz.precise.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class AttributeHierarchyNode extends BaseEntity {
	
	@Column(nullable=false)
	private String value;

	@ManyToOne
	private AttributeHierarchyLevel level;
	
	@ManyToOne
	private AttributeHierarchyNode parent;

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, orphanRemoval=true)
	@MapKey(name="value")
	private Map<String, AttributeHierarchyNode> children = new HashMap<>();
	
	private int units = 1;
	
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
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
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
	
	public Iterator<AttributeHierarchyNode> ancestors() {
		return new AncestorIterator(this);
	}
	
	public Stream<AttributeHierarchyNode> ancestorStream() {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(ancestors(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL),
			false
		);
	}
	
	public Map<String, AttributeHierarchyNode> getChildren() {
		return children;
	}

	public void setChildren(Map<String, AttributeHierarchyNode> children) {
		NodeToMany.CHILDREN.setMany(this, children);
	}
	
	void internalSetChildren(Map<String, AttributeHierarchyNode> children) {
		this.children = children;
		units = children.values().stream()
			.mapToInt(AttributeHierarchyNode::getUnits)
			.sum();
	}
	
	public void addChild(AttributeHierarchyNode node) {
		NodeToMany.CHILDREN.addOneOfMany(this, node);
	}
	
	public AttributeHierarchyNode findChildByValue(String value) {
		return children.get(value);
	}

	public int getUnits() {
		return units;
	}

	public void setUnits(int units) {
		this.units = units;
	}
	
	public boolean isValuesMatchPositions() {
		return valuesMatchPositions;
	}

	public void setValuesMatchPositions(boolean valuesMatchPositions) {
		this.valuesMatchPositions = valuesMatchPositions;
	}
	
	public void countUnits() {
		units = level.isUnit() ? 1
			: level.hasOnlyUnits() ? children.size()
			: children.values().stream()
				.peek(AttributeHierarchyNode::countUnits)
				.mapToInt(AttributeHierarchyNode::getUnits)
				.sum();
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
	
	@Override
	public String toString() {
		return "AttributeHierarchyNode [id=" + getId() + ", value=" + value + ", level=" + level + ", parent=" + parent + "]";
	}

	private static class AncestorIterator implements Iterator<AttributeHierarchyNode> {
			
		private AttributeHierarchyNode next;
		
		private AncestorIterator(AttributeHierarchyNode start) {
			next = start;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public AttributeHierarchyNode next() {
			AttributeHierarchyNode cur = next;
			next = next.getParent();
			return cur;
		}
			
	}
	
}
