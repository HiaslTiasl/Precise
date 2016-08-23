package it.unibz.precise.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Location extends BaseEntity {

	private int level;

	@ManyToOne
	private AttributeHierarchyNode node;
	
	@ManyToOne
	private Task task;

	public Location() {
	}
	
	public Location(AttributeHierarchyNode node, Task task) {
		setNode(node);
		this.task = task;
	}
	
	public AttributeHierarchyNode getNode() {
		return node;
	}
	
	public void setNode(AttributeHierarchyNode node) {
		this.node = node;
		this.level = node == null ? 0 : node.getLevel().getPosition();
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		TaskToMany.LOCATIONS.setOne(this, task);
	}
	
	@Transient
	public Long getTaskID() {
		return task == null ? null : task.getId();
	}
	
	void internalSetTask(Task task) {
		this.task = task;
	}
	
}
