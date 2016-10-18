package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Dependency.LabelPosition;
import it.unibz.precise.model.Position;

@Projection(name="dependencySummary", types=Dependency.class)
public interface DependencySummaryProjection {
	
	long getId();
	
	EmptyProjection getSource();
	
	EmptyProjection getTarget();
	
	Position getSourceVertex();

	Position getTargetVertex();
	
	boolean isAlternate();
	
	boolean isChain();
	
	ScopeSummaryProjection getScope();
	
	List<Position> getVertices();
	
	LabelPosition getLabelPosition();

}
