package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.HasLongId;
import it.unibz.precise.model.Dependency.LabelPosition;
import it.unibz.precise.model.Position;

/**
 * Dependency with source and target as {@link EmptyProjection} to just enable identification,
 * and with scope as {@link ScopeSummaryProjection}.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="dependencySummary", types=Dependency.class)
public interface DependencySummaryProjection extends HasLongId {
	
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
