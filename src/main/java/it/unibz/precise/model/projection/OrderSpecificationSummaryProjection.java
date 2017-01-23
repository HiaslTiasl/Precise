package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;

/**
 * Order specification with attribute as {@link AttributeSummaryProjection}.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="orderSpecificationSummary", types=OrderSpecification.class)
public interface OrderSpecificationSummaryProjection {
	
	AttributeSummaryProjection getAttribute();
	
	OrderType getOrderType();

}
