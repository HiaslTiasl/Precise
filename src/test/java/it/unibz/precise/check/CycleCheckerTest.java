package it.unibz.precise.check;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import it.unibz.precise.Application;
import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.util.Util;

@RunWith(Parameterized.class)
@SpringBootTest(classes=Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
public class CycleCheckerTest {
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Autowired
	private DiagramCycleChecker cycleChecker;
	
	@Parameter(0)
	public String name;
	
	@Parameter(1)
	public List<List<Integer>> adj;

	@Parameter(2)
	public Set<Set<Integer>> expectedSCCs;
	
	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			SCCTestData.emptyGraph().toArray(),
			SCCTestData.dag().toArray(),
			SCCTestData.twoCycles().toArray(),
			SCCTestData.multiplyCyclesPerSCC().toArray()
		});
	}
	
	private List<Task> toTasks(List<List<Integer>> adj) {
		List<Task> tasks = IntStream.range(0, adj.size()).mapToObj(i -> {
			Task t = new Task();
			t.setId(i);
			return t;
		}).collect(Collectors.toList());
		
		IntStream.range(0, adj.size()).forEach(i -> {
			Task source = tasks.get(i);
			List<Dependency> out = Util.mapToList(adj.get(i), j -> {
				Dependency d = new Dependency();
				d.setSource(source);
				d.setTarget(tasks.get(j));
				return d;
			});
			source.setOut(out);
		});
		
		return tasks;
	}
	
	private Set<Set<Task>> toExpectedTaskSets(List<Task> tasks, Set<Set<Integer>> sccs) {
		return sccs.stream()
			.filter(c -> c.size() > 1)
			.map(c -> c.stream().map(tasks::get).collect(Collectors.toSet()))
			.collect(Collectors.toSet());
	}
	
	@Test
	public void testCheck() {
		

		List<Task> tasks = toTasks(adj); 
		
		Model model = new Model();
		model.setTasks(tasks);
		
		Set<Set<Task>> expectedTaskSets = toExpectedTaskSets(tasks, expectedSCCs);

		Set<? extends Set<? extends BaseEntity>> foundSCCs = cycleChecker.check(model)
			.map(w -> w.getEntities()
				.stream()
				.filter(Task.class::isInstance)
				.collect(Collectors.toList())
			)
			.map(es -> new HashSet<>(es))
			.collect(Collectors.toSet());
		
		Assert.assertEquals(expectedTaskSets, foundSCCs);
	}

}
