package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * Represents a location that can be contained in a task.
 * 
 * Refers to a {@link AttributeHierarchyNode} in the hierarchy.
 * Tasks cannot share {@code Location}s, but locations can share
 * {@code AttributeHierarchyNode}s.
 * A numerical level identifies the level in the hierarchy.
 * In particular, a global location has a {@code level == 0} and {@code node == null}.
 * 
 * Future implementations might allow to refer to multiple CAs in one location,
 * e.g. by not restricting the use of wildcards on higher level in the hierarchy.
 * 
 * @author MatthiasP
 *
 */
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
	
	/** Updates {@code level} based on {@code node}. */
	public void update() {
		level = node == null ? 0 : node.getLevel().getPosition();		
	}

	@Override
	public String toString() {
		return "Location [level=" + level + ", node=" + node + "]";
	}
	
}
