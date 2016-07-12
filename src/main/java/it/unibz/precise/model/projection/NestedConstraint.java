package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.Task;

@Projection(name="nested", types=Constraint.class)
public interface NestedConstraint {
	List<Task> getTasks();
}
