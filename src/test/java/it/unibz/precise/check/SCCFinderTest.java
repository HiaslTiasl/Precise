package it.unibz.precise.check;

import java.util.*;

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
import it.unibz.precise.graph.MaterializedGraph;
import it.unibz.util.Util;

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
	public BitSet[] adj;

	@Parameter(2)
	public Set<BitSet> expectedSCCs;
	
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
		SCCFinder.Components foundSCCs = sccFinder.findSCCs(MaterializedGraph.of(adj));
		Assert.assertEquals(expectedSCCs, Util.asSet(Arrays.asList(foundSCCs.asBitSets())));
	}
	
}
