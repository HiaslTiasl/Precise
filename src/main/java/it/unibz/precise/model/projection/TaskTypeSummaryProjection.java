package it.unibz.precise.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.TaskType;

/**
 * Task definition with {@link PhaseSummaryProjection} and number of tasks
 * 
 * @author MatthiasP
 *
 */
@Projection(name="taskTypeSummary", types=TaskType.class)
public interface TaskTypeSummaryProjection {

	String getName();
	
	String getShortName();
	
	String getDescription();
	
	String getUnitOfMeasure();
	
	Craft getCraft();
	
	EmptyProjection getPhase();
	
	@Value("#{target.tasks.size()}")
	int getTaskCount();
	
}
