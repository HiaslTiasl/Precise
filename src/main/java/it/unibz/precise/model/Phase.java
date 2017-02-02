package it.unibz.precise.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.unibz.precise.model.projection.ExpandedPhaseProjection;

/**
 * Represents a construction phase.
 * 
 * Every {@code Phase} has a separate CA hierarchy, represented by an ordered
 * list of {@link AttributeHierarchyLevel}s.
 * 
 * @author MatthiasP
 *
 */
@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(name=Phase.UC_NAME, columnNames={"model_id", "name"})
})
public class Phase extends BaseEntity {
	
	public static final String UC_NAME = "UC_PHASE_NAME";
	
	@Column(nullable=false)
	private String name;
	
	private String description;
	
	@Convert(converter=ColorConverter.class)
	private Color color;

	@OneToMany(mappedBy="phase", cascade=CascadeType.ALL, orphanRemoval=true)
	@OrderBy("position")
	private List<AttributeHierarchyLevel> attributeHierarchyLevels = new ArrayList<>();
	
	private int units;
	
	@ManyToOne
	private Model model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getUnits() {
		return units;
	}

	public List<AttributeHierarchyLevel> getAttributeHierarchyLevels() {
		return attributeHierarchyLevels;
	}

	public void setAttributeHierarchyLevels(List<AttributeHierarchyLevel> attributeHierarchyLevels) {
		PhaseToMany.LEVELS.setMany(this, attributeHierarchyLevels);
		updateLevelPositions();
	}
	
	public void addAttribute(Attribute attribute) {
		AttributeHierarchyLevel level = new AttributeHierarchyLevel();
		level.setAttribute(attribute);
		addLevel(level);
	}

	public void addLevel(AttributeHierarchyLevel level) {
		PhaseToMany.LEVELS.addOneOfMany(this, level);
		level.setPosition(attributeHierarchyLevels.size());
	}
	
	void internalSetLevels(List<AttributeHierarchyLevel> levels) {
		attributeHierarchyLevels = levels;
	}
	
	/** Update positions of levels according to their index. */
	public void updateLevelPositions() {
		IntStream.range(0, attributeHierarchyLevels.size())
			.forEach(i -> attributeHierarchyLevels.get(i).setPosition(i + 1));
	}
	
	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.PHASES.setOne(this, model);
	}
	
	void internalSetModel(Model model) {
		this.model = model;
	}
	
	/** Returns the first {@link AttributeHierarchyLevel} in the hierarchy. */
	public AttributeHierarchyLevel firstLevel() {
		return nextLevel(null);
	}
	
	/**
	 * Returns the {@link AttributeHierarchyLevel} that follows the given {@code prev}.
	 * If {@code null} is passed, the first  {@code AttributeHierarchyLevel} is returned.
	 */
	public AttributeHierarchyLevel nextLevel(AttributeHierarchyLevel prev) {
		int nextIndex = prev == null ? 0 : prev.getPosition();		// N.B. position == index + 1
		return nextIndex < attributeHierarchyLevels.size()
			? attributeHierarchyLevels.get(nextIndex) : null;
	}
	
	/**
	 * Counts the number of all units in the hierarchy.
	 * @see AttributeHierarchyNode#countUnits()
	 */
	public void countUnits() {
		AttributeHierarchyLevel firstLevel = firstLevel();
		units = firstLevel == null ? 0 : firstLevel.getNodes().values().stream()
			.peek(AttributeHierarchyNode::countUnits)		// count units of all top-level CA nodes (which initiates recursion lower-level nodes),
			.mapToInt(AttributeHierarchyNode::getUnits)		// and sum them up
			.sum();
	}
	
	/** Returns a stream of all leaf nodes, i.e. units, in this phase. */
	public Stream<AttributeHierarchyNode> unitsStream() {
		AttributeHierarchyLevel firstLevel = firstLevel();
		return firstLevel == null ? Stream.empty()
			: firstLevel.getNodes().values().stream().flatMap(AttributeHierarchyNode::unitsStream);
	}
	
	/**
	 * Returns a tree representation of the CA hierarchy using the standard.
	 * The tree recursively maps all values for the current level to subtrees corresponding
	 * to the respective children.
	 * Upper levels use the {@link Map} interface for that purpose.
	 * The leaf level is described as a {@link Collection} of the respective values, and
	 * as an integer {@code n} as a shortcut to represent a collection from 1 to {@code n},
	 * if applicable according to {@link AttributeHierarchyNode#isValuesMatchPositions()}.
	 * <p>
	 * One could argue that methods regarding this tree representation belong to classes
	 * that deal with translation between entities and MDL.
	 * However, including them here enables to define projections to this representation
	 * @see ExpandedPhaseProjection
	 */
	@JsonIgnore
	public Object getBuildingTree() {
		AttributeHierarchyLevel firstLevel = firstLevel();
		return firstLevel == null ? null : createTree(firstLevel, firstLevel.getNodes());
	}
	
	/**
	 * Implementation of {@link #buildingTree()} by recursively walking down the hierarchy.
	 * @param level A level in the hierarchy
	 * @param nodes the nodes in the current level that are children of the node currently
	 *        considered by the caller.
	 * @return A {@link Map} from attribute values to children for higher levels, and an integer
	 *         or {@link Collection} for leaf levels depending on
	 *         {@link AttributeHierarchyNode#isValuesMatchPositions()}.
	 * 
	 */
	private Object createTree(AttributeHierarchyLevel level, Map<String, AttributeHierarchyNode> nodes) {
		Map<String, Object> tree = new LinkedHashMap<>();
		Attribute attribute = level.getAttribute();
		AttributeHierarchyLevel nextLevel = level.next();
		boolean hasSubTrees = false;
		for (String v : attribute.getRange()) {
			AttributeHierarchyNode node = nodes.get(v);
			if (node != null) {
				Object subTree = null;
				Map<String, AttributeHierarchyNode> children = node.getChildren();
				if (!children.isEmpty()) {
					hasSubTrees = true;
					if (node.isValuesMatchPositions())
						subTree = children.size();
					else
						subTree = createTree(nextLevel, children);
				}
				tree.put(v, subTree);
			}
		}
		return hasSubTrees ? tree : tree.keySet();
	}
	
	/**
	 * Sets the building tree according to the given tree representation.
	 * Assumes that levels {@link AttributeHierarchyLevel}s been setup already
	 * and establishes the {@link AttributeHierarchyNode}s corresponding to
	 * the given tree.
	 */
	@JsonIgnore
	public void setBuildingTree(Object tree) {
		// Clear old nodes
		attributeHierarchyLevels.forEach(l -> l.setNodes(Collections.emptyMap()));
		walkTree(null, firstLevel(), tree);
	}
	
	/**
	 * Visit {@code value} in {@code level} and process it based on its type.
	 * @see #walkTree(AttributeHierarchyNode, AttributeHierarchyLevel, int)
	 * @see #walkTree(AttributeHierarchyNode, AttributeHierarchyLevel, Collection)
	 * @see #walkTree(AttributeHierarchyNode, AttributeHierarchyLevel, Map)
	 */
	private void walkTree(AttributeHierarchyNode parent, AttributeHierarchyLevel level, Object value) {
		if (level != null) {
			if (value instanceof Integer)
				walkTree(parent, level, (int)value);
			else if (value instanceof Collection)
				walkTree(parent, level, (Collection<?>)value);
			else if (value instanceof Map)
				walkTree(parent, level, (Map<?,?>)value);
		}
	}
	
	/** Visit an integer leaf node: shortcut for nodes with values from 1 to {@code totalNum}. */
	private void walkTree(AttributeHierarchyNode parent, AttributeHierarchyLevel level, int totalNum) {
		Attribute attr = level.getAttribute();
		// No parent if whole tree is a single integer.
		// Otherwise, remember that this was an integer.
		if (parent != null)
			parent.setValuesMatchPositions(true);
		for (int value = 1; value <= totalNum; value++)
			createNode(parent, level, attr.checkValue(value));
	}
	
	/** Visit an collection of leaf nodes. */
	private void walkTree(AttributeHierarchyNode parent, AttributeHierarchyLevel level, Collection<?> leafs) {
		Attribute attr = level.getAttribute();
		for (Object value : leafs)
			createNode(parent, level, attr.checkValue(value));
	}
	
	/** Visit a map from values to children. */
	private void walkTree(AttributeHierarchyNode parent, AttributeHierarchyLevel level, Map<?, ?> tree) {
		Attribute attr = level.getAttribute();
		for (Entry<?, ?> e : ((Map<?, ?>)tree).entrySet()) {
			// e maps a value to its children
			String v = attr.checkValue(e.getKey());
			AttributeHierarchyNode node = createNode(parent, level, v);
			// Recursion with next level and children of node.
			walkTree(node, level.next(), e.getValue());
		}
	}
	
	/**
	 * Creates a new {@link AttributeHierarchyNode} of the given {@code level} and {@code value}
	 * and adds it to {@code parent} if available.
	 */
	private AttributeHierarchyNode createNode(AttributeHierarchyNode parent, AttributeHierarchyLevel level, String value) {
		AttributeHierarchyNode node = new AttributeHierarchyNode(value);
		level.addNode(node);
		if (parent != null)
			parent.addChild(node);
		return node;
	}
	
	/** Update dependent fields before persisting changes. */
	@PrePersist
	@PreUpdate
	public void preSave() {
		updateLevelPositions();
		countUnits();
	}

	@Override
	public String toString() {
		return "Phase [id=" + getId() + ", name=" + name + "]";
	}
	
}
