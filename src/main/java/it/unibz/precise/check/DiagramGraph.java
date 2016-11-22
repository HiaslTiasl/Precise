package it.unibz.precise.check;

import java.util.Collection;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

public class DiagramGraph extends Graph<Task> {

	public DiagramGraph(Collection<Task> tasks) {
		super(tasks, t -> t.getOut().stream().map(Dependency::getTarget));
	}
	
	public static DiagramGraph of(Collection<Task> tasks) {
		return new DiagramGraph(tasks);
	}
	
	public static DiagramGraph of(Model model) {
		return new DiagramGraph(model.getTasks());
	}

}
