package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Scope;
/**
 * Scope with attributes as {@link AttributeSummaryProjection}.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="scopeSummary", types=Scope.class)
public interface ScopeSummaryProjection {

	Scope.Type getType();
	
	List<AttributeSummaryProjection> getAttributes();

}
