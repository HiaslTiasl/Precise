package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * Part of an ordering constraint of a {@link Task}.
 * 
 * Orderings are defined as ordered list of {@code OrderSpecification}s,  
 * which associate {@link Attribute}s to a {@link OrderType}s.
 * 
 * @author MatthiasP
 *
 */
@Embeddable
public class OrderSpecification {
	
	@ManyToOne
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

	@Override
	public String toString() {
		return "OrderSpecification [attribute=" + attribute + ", orderType=" + orderType + "]";
	}
	
}
