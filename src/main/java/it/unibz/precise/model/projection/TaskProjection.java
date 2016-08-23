package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

@Projection(name="task", types=Task.class)
public interface TaskProjection {
	
	int getId();

	float getNumberOfWorkersNeeded();
	
	float getNumberOfUnitsPerDay();
	
	boolean isGlobalExclusiveness();
	
	List<Attribute> getExclusiveness();

	List<Location> getLocations();
	
	TaskType getType();
	
	Position getPosition();
}
