package it.unibz.precise.data;

import java.util.Arrays;
import java.util.List;

import it.unibz.precise.model.Configuration;
import it.unibz.precise.model.FlowConstraint;
import it.unibz.precise.model.FlowConstraint.Kind;
import it.unibz.precise.model.ConstructionUnit;
import it.unibz.precise.model.Flow;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

public class DataSources {

	// TODO: replace with real one, find better decoupling
	private static DataSource instance = new ManagedDataSource();
	
	public static DataSource getDefault() {
		return instance;
	}
	
	public static List<Model> getDummyData() {
		TaskType tta = new TaskType("Do A");
		TaskType ttb = new TaskType("Do B");
		List<TaskType> taskTypes = Arrays.asList(tta, ttb);
		
		ConstructionUnit r1 = new ConstructionUnit("A", "l1", "r", 1);
		ConstructionUnit r2 = new ConstructionUnit("A", "l1", "r", 2);
		List<ConstructionUnit> units = Arrays.asList(r1, r2);
		
		Configuration config = new Configuration(taskTypes, units);
		
		Task ta = new Task(tta, units, Scope.TASK);
		Task tb = new Task(ttb, units, Scope.UNIT);
		List<Task> tasks = Arrays.asList(ta, tb);
		
		FlowConstraint c1 = new FlowConstraint(Kind.EXCLUSIVE_EXISTENCE, Scope.LEVEL, ta);
		FlowConstraint c2 = new FlowConstraint(Kind.PRECEDENCE, Scope.UNIT, ta, tb);
		 
		Flow f1 = new Flow(tasks, Arrays.asList(c1));
		Flow f2 = new Flow(tasks, Arrays.asList(c2));
		
		Model m1 = new Model(config, f1);
		Model m2 = new Model(config, f2);
		
		return Arrays.asList(m1, m2);
	}
	
}

