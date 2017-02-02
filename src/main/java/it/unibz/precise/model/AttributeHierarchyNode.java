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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Represents a node in the CA hierarchy, i.e. a CA.
 * Has an {@link AttributeHierarchyLevel}, a value for the corresponding attribute,
 * and a parent {@code AttributeHierarchyNode} of the level above, if any, and children
 * {@code AttributeHierarchyNode}s of the next level, if any.
 * 
 * @author MatthiasP
 *
 */
@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(name=AttributeHierarchyNode.UC_PARENT_VALUE, columnNames={"parent_id", "value"}),
	//@UniqueConstraint(name=AttributeHierarchyNode.UC_LEVEL_VALUE, columnNames={"level_id", "value"})
})
public class AttributeHierarchyNode extends BaseEntity {
	
	public static final String UC_PARENT_VALUE = "UC_NODE_PARENT_VALUE";
	//public static final String UC_LEVEL_VALUE = "UC_NODE_LEVEL_VALUE";
	
	@Column(nullable=false)
	private String value;						// The particular value of this node

	@ManyToOne
	private AttributeHierarchyLevel level;		// The level of the node in the hierarchy, determines phase and attribute
	
	@ManyToOne
	private AttributeHierarchyNode parent;		// The parent node

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, orphanRemoval=true)
	@MapKey(name="value")
	private Map<String, AttributeHierarchyNode> children = new HashMap<>();		// Children nodes
	
	private int units = 1;						// Total number of units (leaf nodes) contained
	
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

	public AttributeHierarchyNode getParent() {
		return parent;
	}

	public void setParent(AttributeHierarchyNode parent) {
		NodeToMany.CHILDREN.setOne(this, parent);
	}
	
	void internalSetParent(AttributeHierarchyNode parent) {
		this.parent = parent;
	}
	
	/** Returns an iterator for traversing through the {@link #getParent() parent} relation until null is reached. */
	public Iterator<AttributeHierarchyNode> ancestors() {
		return new AncestorIterator(this);
	}
	
	/** Returns an ordered ancestor nodes, starting from the current node and terminating with the root node. */
	public Stream<AttributeHierarchyNode> ancestorStream() {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(ancestors(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL),
			false
		);
	}
	
	/** Returns a stream of leaf nodes, i.e. units, under this node. */
	public Stream<AttributeHierarchyNode> unitsStream() {
		return level.isUnit() ? Stream.of(this)
			: level.hasOnlyUnits() ? children.values().stream()
			: children.values().stream().flatMap(AttributeHierarchyNode::unitsStream);
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
	
	/** Recursively updates {@link #units}. */
	public void countUnits() {
		// N.B. The current model does not assume a "unit-like" leaf level, i.e. with values from 1 to n.
		// Also, special cases such as hierarchies with a single atribute only must be considered here.
		units = level.isUnit() ? 1
			: level.hasOnlyUnits() ? children.size()
			: children.values().stream()
				.peek(AttributeHierarchyNode::countUnits)
				.mapToInt(AttributeHierarchyNode::getUnits)
				.sum();
	}
	
	/** Returns a map representation for this node, mapping attribute names to {@link PatternEntry}s. */
	public Map<String, PatternEntry> getPattern() {
		return toPattern(this, level.getPhase().getAttributeHierarchyLevels());
	}
	
	/**
	 * Returns a map representation for the given node and levels.
	 * If {@code node == null}, a pattern corresponding to a global location is returned,
	 * i.e. all attributes are mapped to {@link PatternEntry#WILDCARD_VALUE}.
	 */
	public static Map<String, PatternEntry> toPattern(AttributeHierarchyNode node, List<AttributeHierarchyLevel> levels) {
		Map<String, PatternEntry> pattern = new LinkedHashMap<>();
		if (node != null)
			node.addAncestorsTo(pattern);
		addWildcardsTo(pattern, levels);
		return pattern;
	}
	
	/** Add a mapping to the given pattern. */
	private static void addToPattern(Map<String, PatternEntry> pattern, Attribute attribute, String value) {
		String attrName = attribute.getName();
		pattern.put(attrName, new PatternEntry(attrName, value));
	}
	
	/** Add a mapping for the node's level and value to the given pattern. */
	private void addToPattern(Map<String, PatternEntry> pattern) {
		addToPattern(pattern, level.getAttribute(), value);
	}
	
	/** Recursively add mappings for all ancestors, beginning with root node. */
	private void addAncestorsTo(Map<String, PatternEntry> pattern) {
		if (parent != null)
			parent.addAncestorsTo(pattern);
		addToPattern(pattern);
	}
	
	/** Map attributes to {@link PatternEntry#WILDCARD_VALUE} if not mapped to an actual value. */
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

	/** An iterator traversing through the {@link #getParent() parent} relation until null is reached. */
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
