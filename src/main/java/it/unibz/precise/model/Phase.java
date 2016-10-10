package it.unibz.precise.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"})
})
public class Phase extends BaseEntity {
	
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
	}
	
	public void addAttribute(Attribute attribute) {
		AttributeHierarchyLevel level = new AttributeHierarchyLevel();
		level.setAttribute(attribute);
		addLevel(level);
	}

	public void addLevel(AttributeHierarchyLevel level) {
		PhaseToMany.LEVELS.addOneOfMany(this, level);
	}
	
	void internalSetLevels(List<AttributeHierarchyLevel> levels) {
		attributeHierarchyLevels = levels;
	}
	
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
	
	public AttributeHierarchyLevel firstLevel() {
		return nextLevel(null);
	}
	
	public AttributeHierarchyLevel nextLevel(AttributeHierarchyLevel prev) {
		int nextIndex = prev == null ? 0 : prev.getPosition();		// N.B. position == index + 1
		return nextIndex < attributeHierarchyLevels.size()
			? attributeHierarchyLevels.get(nextIndex) : null;
	}
	
	public void countUnits() {
		AttributeHierarchyLevel firstLevel = firstLevel();
		units = firstLevel == null ? 0 : firstLevel.getNodes().values().stream()
			.peek(AttributeHierarchyNode::countUnits)
			.mapToInt(AttributeHierarchyNode::getUnits)
			.sum();
	}
	
	public Object buildingTree() {
		AttributeHierarchyLevel firstLevel = firstLevel();
		return firstLevel == null ? null : createTree(firstLevel, firstLevel.getNodes());
	}
	
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
	
	@PrePersist
	@PreUpdate
	public void preSave() {
		updateLevelPositions();
		countUnits();
	}
	
}
