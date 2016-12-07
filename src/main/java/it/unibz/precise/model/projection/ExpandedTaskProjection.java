package it.unibz.precise.model.projection;

import java.util.List;
import java.util.Map;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;

@Projection(name="expandedTask", types=Task.class)
public interface ExpandedTaskProjection {
	
	long getId();
	
	int getUnits();
	
	Pitch getPitch();
	
	ScopeSummaryProjection getExclusiveness();

	List<OrderSpecificationSummaryProjection> getOrderSpecifications();

	List<Map<String, PatternEntry>> getLocationPatterns();
	
	ExpandedTaskTypeProjection getType();
	
	Position getPosition();
}
