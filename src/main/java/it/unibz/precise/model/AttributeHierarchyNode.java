package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class AttributeHierarchyNode extends BaseEntity {

	@ManyToOne
	private AttributeHierarchyLevel level;
	
	@ManyToOne
	private AttributeHierarchyNode parent;

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	private List<AttributeHierarchyNode> children = new ArrayList<>();
	
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
		this.children = new ArrayList<>();
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
	
	public List<AttributeHierarchyNode> getChildren() {
		return children;
	}

	public void setChildren(List<AttributeHierarchyNode> children) {
		NodeToMany.CHILDREN.setMany(this, children);
	}
	
	void internalSetChildren(List<AttributeHierarchyNode> children) {
		this.children = children;
	}
	
	public void addChild(AttributeHierarchyNode node) {
		NodeToMany.CHILDREN.addOneOfMany(this, node);
	}
	
	void internalAddChild(AttributeHierarchyNode node) {
		children.add(node);
	}
	
	public AttributeHierarchyNode findChildByValue(String value) {
		return findByValue(children, value);
	}
	
	public static AttributeHierarchyNode findByValue(List<AttributeHierarchyNode> nodes, String value) {
		return value == null ? null : nodes.stream()
			.filter(c -> value.equals(c.getValue()))
			.findAny()
			.orElse(null);
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
	
}
