package it.unibz.precise.rest.mdl.ast;

import it.unibz.precise.model.OrderType;

public class MDLOrderSpecificationAST {

	private MDLAttributeAST attribute;
	private OrderType orderType;
	
	public MDLAttributeAST getAttribute() {
		return attribute;
	}
	
	public void setAttribute(MDLAttributeAST attribute) {
		this.attribute = attribute;
	}
	
	public OrderType getOrderType() {
		return orderType;
	}
	
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

}
