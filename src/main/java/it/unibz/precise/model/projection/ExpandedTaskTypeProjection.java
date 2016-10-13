package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.TaskType;

@Projection(name="expandedTaskType", types=TaskType.class)
public interface ExpandedTaskTypeProjection {

	String getName();
	
	String getDescription();
	
	Craft getCraft();
	
	PhaseSummaryProjection getPhase();
	
}
