package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Position;

@Projection(name="dependencySummary", types=Dependency.class)
public interface DependencySummaryProjection {
	
	long getId();
	
	Long getSourceID();
	
	Long getTargetID();
	
	Position getSourceVertex();

	Position getTargetVertex();
	
	boolean isAlternate();
	
	boolean isChain();
	
	boolean isGlobalScope();
	
	List<AttributeSummaryProjection> getScope();
	
	List<Position> getVertices();
}
