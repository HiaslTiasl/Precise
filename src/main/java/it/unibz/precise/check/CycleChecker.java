package it.unibz.precise.check;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.util.Util;

@Service
public class CycleChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "cycles";
	
	public static final String WARNING_MESSAGE = "The following tasks contain cycles: ";

	@Autowired
	private SCCFinder sccFinder;
	
	@Override
	public List<ConsistencyWarning> check(Model model) {
		List<Task> tasks = model.getTasks();
		List<List<Integer>> adj = AdjacencyLists.from(tasks);
		List<List<Integer>> sccs = sccFinder.findSCCs(adj);
		return sccs.stream()
			.filter(c -> c.size() > 1)
			.map(c -> Util.mapToList(c, tasks::get))
			.map(this::warning)
			.collect(Collectors.toList());
	}
	
	private ConsistencyWarning warning(List<Task> tasks) {
		Collections.reverse(tasks);
		String msg = WARNING_MESSAGE
			+ tasks.stream()
				.map(Task::getId)
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
		return new ConsistencyWarning(WARNING_TYPE, msg, tasks);
	}
	
	// Usage example
	public static void main(String[] args) {
		
		List<List<Integer>> g = Arrays.asList(
			Arrays.asList(1),
			Arrays.asList(2),
			Arrays.asList(0, 3),
			Arrays.asList(4),
			Arrays.asList(5),
			Arrays.asList(4)
		);
		
		List<Task> tasks = IntStream.range(0, g.size()).mapToObj(i -> {
			Task t = new Task();
			t.setId(i);
			return t;
		}).collect(Collectors.toList());
		
		IntStream.range(0, g.size()).forEach(i -> {
			Task source = tasks.get(i);
			List<Dependency> out = Util.mapToList(g.get(i), j -> {
				Dependency d = new Dependency();
				d.setSource(source);
				d.setTarget(tasks.get(j));
				return d;
			});
			source.setOut(out);
		});
		
		Model model = new Model();
		model.setTasks(tasks);

		new CycleChecker().check(model).stream().map(ConsistencyWarning::getMessage).forEach(System.out::println);
	}
	
}