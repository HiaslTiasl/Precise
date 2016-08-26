package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Position;

@Projection(name="dependencySummary", types=Dependency.class)
public interface DependencySummaryProjection {
	
	int getId();
	
	boolean isAlternate();
	
	boolean isChain();
	
	int getSourceID();

	int getTargetID();
	
	List<AttributeSummaryProjection> getScope();
	
	List<Position> getVertices();
}
