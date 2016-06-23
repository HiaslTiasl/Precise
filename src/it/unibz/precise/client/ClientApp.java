package it.unibz.precise.client;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;

import it.unibz.precise.model.Configuration;
import it.unibz.precise.model.FlowConstraint;
import it.unibz.precise.model.FlowConstraint.Kind;
import it.unibz.precise.model.ConstructionUnit;
import it.unibz.precise.model.Flow;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

public class ClientApp {
	
	public static void main(String[] args) {
		Client client = ClientBuilder.newClient();
	    WebTarget target = client.target("http://localhost:8080/Construction/model");
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
		
		StatusType resInfo1 = target.request()
			.post(Entity.entity(m1, MediaType.APPLICATION_JSON_TYPE))
			.getStatusInfo();
		StatusType resInfo2 = target.request()
				.post(Entity.entity(m2, MediaType.APPLICATION_JSON_TYPE))
				.getStatusInfo();
		
		System.out.println(resInfo1);
		System.out.println(resInfo2);
	}
	
}
