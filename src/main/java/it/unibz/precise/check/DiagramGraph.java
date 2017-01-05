package it.unibz.precise.check;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

import java.util.Collection;
import java.util.Objects;

public class DiagramGraph extends Graph<Task> {

	public DiagramGraph(Collection<Task> tasks) {
		super(
			tasks,
			t -> t.getOut().stream()
				.map(Dependency::getTarget)
				.filter(Objects::nonNull)
		);
	}
	
	public static DiagramGraph of(Collection<Task> tasks) {
		return new DiagramGraph(tasks);
	}
	
	public static DiagramGraph of(Model model) {
		return new DiagramGraph(model.getTasks());
	}

}
