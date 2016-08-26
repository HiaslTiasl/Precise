package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Location;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

@Projection(name="fullTask", types=Task.class)
public interface TaskProjection {
	
	int getId();

	float getNumberOfWorkersNeeded();
	
	float getNumberOfUnitsPerDay();
	
	boolean isGlobalExclusiveness();
	
	List<AttributeSummaryProjection> getExclusiveness();

	List<Location> getLocations();
	
	TaskType getType();
	
	@Value("#{target.type.phase.attributeHierarchyLevels.size()}")
	int getHierarchyDepth();
	
	Position getPosition();
}
