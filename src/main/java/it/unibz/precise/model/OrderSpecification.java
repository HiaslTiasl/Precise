package it.unibz.precise.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class OrderSpecification extends BaseEntity {
	
	@ManyToOne
	private Attribute attribute;
	
	private OrderType orderType;
	
	@ManyToOne
	private Task task;
	
	public OrderSpecification() {
	}

	public OrderSpecification(Attribute attribute, OrderType orderType) {
		this.attribute = attribute;
		this.orderType = orderType;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		TaskToMany.ORDER_SPECIFICATIONS.setOne(this, task);
	}
	
	void internalSetTask(Task task) {
		this.task = task;
	}
}
