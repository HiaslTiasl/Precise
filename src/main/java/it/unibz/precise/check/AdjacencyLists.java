package it.unibz.precise.check;

import java.util.List;
import java.util.Map;

import it.unibz.precise.model.Task;
import it.unibz.util.Util;

public class AdjacencyLists {
	
	public static List<List<Integer>> from(List<Task> tasks) {
		Map<Task, Integer> indices = Util.mapToIndex(tasks);
		
		return Util.mapToList(
			tasks, 
			t -> Util.mapToList(
				t.getOut(),
				d -> indices.get(d.getTarget())
			)
		);
	}

}
