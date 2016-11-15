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
import it.unibz.precise.graph.Graph;

@RunWith(Parameterized.class)
@SpringBootTest(classes=Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(classes=ConsistencyConfig.class)
public class SCCFinderTest {
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Autowired
	private SCCFinder sccFinder;
	
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
	
	@Test
	public void testFindSCCs() {
		Graph<Integer> graph = new Graph<>(
			IntStream.range(0, adj.size()).boxed().collect(Collectors.toList()),
			i -> adj.get(i).stream()
		);
		List<List<Integer>> foundSCCs = sccFinder.findSCCs(graph);
		
		Set<Set<Integer>> sccSets = foundSCCs.stream()
			.map(HashSet<Integer>::new)
			.collect(Collectors.toSet());
		
		Assert.assertEquals(expectedSCCs, sccSets);
	}
	
}
