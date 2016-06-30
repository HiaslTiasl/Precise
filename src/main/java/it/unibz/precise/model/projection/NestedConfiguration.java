package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Configuration;
import it.unibz.precise.model.ConstructionUnit;
import it.unibz.precise.model.TaskType;

@Projection(name="nested", types=Configuration.class)
public interface NestedConfiguration {
	List<TaskType> getTaskTypes();
	List<ConstructionUnit> getConstructionUnit();
}
