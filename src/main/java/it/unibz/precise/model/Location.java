package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class Location {

	private int level;
	
	@ManyToOne
	private AttributeHierarchyNode node;

	public Location() {
	}
	
	public Location(AttributeHierarchyNode node) {
		setNode(node);
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public AttributeHierarchyNode getNode() {
		return node;
	}
	
	public void setNode(AttributeHierarchyNode node) {
		this.node = node;
	}
	
	public void update() {
		level = node == null ? 0 : node.getLevel().getPosition();		
	}
	
}
