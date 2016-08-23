package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.TaskType;

@Projection(name="taskType", types=TaskType.class)
public interface TaskTypeProjection {

	String getName();
	
	String getCraft();
	
}
