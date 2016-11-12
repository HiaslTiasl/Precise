package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.rest.mdl.ast.MDLOrderSpecificationAST;

public class OrderSpecificationTranslator extends AbstractMDLTranslator<OrderSpecification, MDLOrderSpecificationAST> {
	
	public OrderSpecificationTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public OrderSpecification createEntity(MDLOrderSpecificationAST mdlOrderSpec) {
		return new OrderSpecification();
	}

	@Override
	public MDLOrderSpecificationAST createMDL(OrderSpecification orderSpec) {
		return new MDLOrderSpecificationAST();
	}

	@Override
	protected void updateEntityImpl(MDLOrderSpecificationAST mdlOrderSpec, OrderSpecification orderSpec) {
		if (mdlOrderSpec != null) {
			orderSpec.setOrderType(mdlOrderSpec.getOrderType());
			orderSpec.setAttribute(context().attributes().toEntity(mdlOrderSpec.getAttribute()));
		}
	}

	@Override
	protected void updateMDLImpl(OrderSpecification orderSpec, MDLOrderSpecificationAST mdlOrderSpec) {
		if (orderSpec != null) {
			mdlOrderSpec.setOrderType(orderSpec.getOrderType());
			mdlOrderSpec.setAttribute(context().attributes().toMDL(orderSpec.getAttribute()));		
		}
	}

}
