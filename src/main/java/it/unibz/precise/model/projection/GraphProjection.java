package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

@Projection(name="graph", types=Model.class)
public interface GraphProjection {
	
	List<Attribute> getAttributes();
	List<Phase> getPhases();
	List<TaskType> getTaskTypes();
	List<Task> getTasks();
	List<Dependency> getDependencies();

}
