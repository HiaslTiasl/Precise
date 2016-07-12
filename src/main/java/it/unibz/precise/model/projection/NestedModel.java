package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Model;

@Projection(name="nested", types=Model.class)
public interface NestedModel {
//	List<TaskType> getTaskTypes();
//	List<ConstructionUnit> getConstructionUnits();
//	List<NestedTask> getTasks();
//	List<NestedConstraint> getConstraints();
}
