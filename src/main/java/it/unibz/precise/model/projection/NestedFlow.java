package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Flow;
import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.Task;

@Projection(name="nested", types=Flow.class)
public interface NestedFlow {
	List<Task> getTasks();
	List<Constraint> getConstraint();
}