package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.rest.mdl.ast.MDLOrderSpecificationAST;

/**
 * {@link MDLTranslator} for order specifications.
 * 
 * @author MatthiasP
 *
 */
public class OrderSpecificationTranslator extends AbstractMDLTranslator<OrderSpecification, MDLOrderSpecificationAST> {
	
	OrderSpecificationTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public OrderSpecification createEntity() {
		return new OrderSpecification();
	}

	@Override
	public MDLOrderSpecificationAST createMDL() {
		return new MDLOrderSpecificationAST();
	}

	@Override
	protected void updateEntityImpl(MDLOrderSpecificationAST mdlOrderSpec, OrderSpecification orderSpec) {
		orderSpec.setOrderType(mdlOrderSpec.getOrderType());
		orderSpec.setAttribute(context().attributes().toEntity(mdlOrderSpec.getAttribute()));
	}

	@Override
	protected void updateMDLImpl(OrderSpecification orderSpec, MDLOrderSpecificationAST mdlOrderSpec) {
		mdlOrderSpec.setOrderType(orderSpec.getOrderType());
		mdlOrderSpec.setAttribute(context().attributes().toMDL(orderSpec.getAttribute()));		
	}

}
