package it.unibz.precise.check;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagramCycleChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "cycles";
	
	public static final String WARNING_MESSAGE = "The following tasks contain cycles: {0}";

	@Autowired
	private SCCFinder sccFinder;
	
	@Override
	public Stream<ConsistencyWarning> check(Model model) {
		List<List<Task>> sccs = sccFinder.findSCCs(DiagramGraph.of(model));
		return sccs.stream()
			.filter(SCCFinder::isNonTrivialComponent)
			.map(this::warning);
	}
	
	@Override
	public Category getCategory() {
		return Category.SATISFIABILITY;
	}
	
	@Override
	public String getType() {
		return WARNING_TYPE;
	}
	
	private ConsistencyWarning warning(List<Task> tasks) {
		Collections.reverse(tasks);
		Set<Task> taskSet = new HashSet<>(tasks);
		
		Stream<Dependency> dependencies = tasks.stream()
			.map(Task::getOut)
			.flatMap(List::stream)
			.filter(d -> taskSet.contains(d.getTarget()));
		
		List<BaseEntity> entities = Stream.concat(tasks.stream(), dependencies).collect(Collectors.toList());
		
		String msg = MessageFormat.format(WARNING_MESSAGE, 
			tasks.stream()
				.sorted(Task.shortIdentificationComparator())
				.map(Task::getShortIdentification)
				.map(String::valueOf)
				.collect(Collectors.joining(", "))
		);
		
		return warning(msg, entities, null);
	}
	
}
