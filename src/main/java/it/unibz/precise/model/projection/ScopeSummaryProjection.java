package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Scope;

@Projection(name="scopeSummary", types=Scope.class)
public interface ScopeSummaryProjection {

	Scope.Type getType();
	
	List<AttributeSummaryProjection> getAttributes();

}
