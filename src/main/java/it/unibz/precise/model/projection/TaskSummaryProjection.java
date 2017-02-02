package it.unibz.precise.model.projection;

import java.util.List;
import java.util.Map;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.HasLongId;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;

/**
 * Task with definition as {@link EmptyProjection} only to provide just enough
 * information for joining on the client.
 * 
 * TODO: Consider using this instead of {@link ExpandedTaskProjection} for initializing
 * the diagram to reduce the time needed.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="taskSummary", types=Task.class)
public interface TaskSummaryProjection extends HasLongId {
	
	long getId();
	
	int getUnits();
	
	Pitch getPitch();
	
	ScopeSummaryProjection getExclusiveness();

	List<OrderSpecificationSummaryProjection> getOrderSpecifications();

	List<Map<String, PatternEntry>> getLocationPatterns();
	
	EmptyProjection getType();
	
	Position getPosition();
}
