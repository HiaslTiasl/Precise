package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class OrderSpecification {
	
	@ManyToOne()
	private Attribute attribute;
	
	private OrderType orderType = OrderType.NONE;
	
	public OrderSpecification() {
	}

	public OrderSpecification(Attribute attribute) {
		this.attribute = attribute;
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
	
}
