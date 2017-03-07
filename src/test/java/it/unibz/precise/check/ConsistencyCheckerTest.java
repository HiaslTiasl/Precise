package it.unibz.precise.check;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
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
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibz.precise.Application;
import it.unibz.precise.TestUtil;
import it.unibz.precise.graph.disj.AcyclicOrientationFinder;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

@RunWith(Parameterized.class)
@SpringBootTest(classes=Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
public class ConsistencyCheckerTest {
	
	private static final int ITERATIONS = 100;
	private static final int WARMUP_ITERATIONS = 100;
	private static final int TIMEOUT_MIN = 60;
	private static final int TIMEOUT_MS = TIMEOUT_MIN * 60 * 1000;
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
	private ModelToGraphTranslator modelToGraphTranslator;
	
	@Autowired
	private AcyclicOrientationFinder orientationFinder;
	
	@Parameter(0)
	public String modelName;
	@Parameter(1)
	public boolean expectSuccess;
	@Parameter(2)
	public boolean ignoreSimpleEdges;
	@Parameter(3)
	public boolean usePartitioning;
	@Parameter(4)
	public boolean useResolving;
	
	private Model model;
	private DisjunctiveGraph<TaskUnitNode> graph;
	
	private long transTimeNs = 0;
	private long checkTimeNs = 0;
	
	private int completedIterations = 0;
	
	private static final List<ConsistencyCheckerTest> allRuns = new ArrayList<>();
	
	@AfterClass
	public static void printRuns() {
		System.out.println("|             model | tasks | dependencies |   nodes |   arcs |   edges | no simple edges | partitioning | resolving || translation |       check |       total |");
		System.out.println("+-------------------+-------+--------------+---------+--------+---------+-----------------+--------------+-----------++-------------+-------------+-------------|");
		for (ConsistencyCheckerTest run : allRuns) {
			System.out.printf(
				"| %17s | % 4d | % 12d | % 7d | % 7d | % 7d |               %c |            %c |         %c || %11s | %11s | %11s |\n",
				run.modelName,
				run.model.getTasks().size(),
				run.model.getDependencies().size(),
				run.graph.nodes().size(),
				run.graph.arcs().size(),
				run.graph.edges().size(),
				run.ignoreSimpleEdges ? 'X' : ' ',
				run.usePartitioning ? 'X' : ' ',
				run.useResolving ? 'X' : ' ',
				run.transTimeCell(),
				run.checkTimeCell(),
				run.totalTimeCell()
			);
		}
	}
	
	@Parameters(name = "{0} ({2}, {3}, {4})")
	public static Collection<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		String[] modelNames = { "test - consistent", "test - cyclic", "test - deadlock" };
		boolean[] expectSuccess = { true, false, false };
		
		for (int i = 0; i < modelNames.length; i++) {
			String m = modelNames[i];
			boolean e = expectSuccess[i];
			// Put sophisticated first so the JIT will optimize them less,
			// thus if they still take less time it is not because of the JIT. 
			params.add(new Object[] { m, e,  true,  true,  true });
			params.add(new Object[] { m, e, false,  true,  true });
			params.add(new Object[] { m, e,  true,  true, false });
			params.add(new Object[] { m, e, false,  true, false });
			params.add(new Object[] { m, e,  true, false,  true });
			params.add(new Object[] { m, e, false, false,  true });
			params.add(new Object[] { m, e,  true, false, false });
			params.add(new Object[] { m, e, false, false, false });
		}
		return params;
	}
	
	@Before
	public void setUp() throws Exception {
		new TestContextManager(getClass()).prepareTestInstance(this);
		MDLFileAST mdl = objectMapper.readValue(TestUtil.load("consistency/" + modelName + ".mdl"), MDLFileAST.class);
		model = MDLContext.create().files().toEntity(mdl);
		warmUp();
	}
	
	private void warmUp() {
		for (int i = 0; i < WARMUP_ITERATIONS; i++) {
			graph = modelToGraphTranslator.translate(model.getTasks(), ignoreSimpleEdges);
			orientationFinder.init(true, true).search(graph).isSuccessful();
		}
	}
	
	@Test(timeout=TIMEOUT_MS)
	public void test() throws JsonParseException, IOException {
		// Add first to also consider timed-out runs
		allRuns.add(this);
		for (int i = 0; i < ITERATIONS; i++) {
			long t0 = System.nanoTime();
			graph = modelToGraphTranslator.translate(model.getTasks(), ignoreSimpleEdges);
			long t1 = System.nanoTime();
			boolean success = orientationFinder.init(usePartitioning, useResolving).search(graph).isSuccessful();
			long t2 = System.nanoTime();
			transTimeNs += t1 - t0;
			checkTimeNs += t2 - t1;
			completedIterations++;
			
			if (expectSuccess)
				assertTrue(success);
			else
				assertFalse(success);
		}
	}
	
	private String transTimeCell() {
		return timeCell(avgTimeMs(transTimeNs));
	}
	
	private String checkTimeCell() {
		return timeCell(avgTimeMs(checkTimeNs));
	}
	
	private String totalTimeCell() {
		return timeCell(avgTimeMs(transTimeNs + checkTimeNs));
	}
	
	private long avgTimeMs(long sumNs) {
		return completedIterations > 0
			? sumNs / ITERATIONS / 1000000
			: -1;
	}
	
	private String timeCell(long ms) {
		return ms > 0 ? String.format("%,d", ms) : "> " + TIMEOUT_MIN + " min";
	}
}
