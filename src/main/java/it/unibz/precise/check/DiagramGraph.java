package it.unibz.precise.check;

import it.unibz.precise.graph.Graph;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

public class DiagramGraph extends Graph<Task> {

	public DiagramGraph(Model model) {
		super(model.getTasks(), t -> t.getOut().stream().map(Dependency::getTarget));
	}

}
