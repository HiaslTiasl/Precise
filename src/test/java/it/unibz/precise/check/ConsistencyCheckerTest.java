package it.unibz.precise.check;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
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
	
	private static final int TIMEOUT_MIN = 60;
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
    public Timeout globalTimeout = Timeout.seconds(TIMEOUT_MIN * 60);
    
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
	
	@Parameter(5)
	public int warmup;
	@Parameter(6)
	public int iterations;
	
	private Model model;
	private DisjunctiveGraph<TaskUnitNode> graph;
	
	private volatile AtomicLong transTimeNs = new AtomicLong();
	private volatile AtomicLong checkTimeNs = new AtomicLong();
	private volatile AtomicInteger completedTranslations = new AtomicInteger();
	private volatile AtomicInteger completedChecks = new AtomicInteger();
	
	private static final List<ConsistencyCheckerTest> allRuns = new ArrayList<>();
	
	@AfterClass
	public static void printRuns() {
		System.out.println("|                   model | tasks | dependencies |   nodes |    arcs |   edges | ISE | R | P || translation |       check |       total | iterations |");
		System.out.println("+-------------------------+-------+--------------+---------+---------+---------+-----+---+---++-------------+-------------+-------------+------------+");
		for (ConsistencyCheckerTest run : allRuns) {
			System.out.printf(
				"| %23s | % 5d | % 12d | % 7d | % 7d | % 7d |  %c  | %c | %c || %11s | %11s | %11s | %10s |\n",
				run.modelName,
				run.model.getTasks().size(),
				run.model.getDependencies().size(),
				run.graph.nodes().size(),
				run.graph.arcCount(),
				run.graph.edges().size(),
				run.ignoreSimpleEdges ? 'X' : ' ',
				run.useResolving ? 'X' : ' ',
				run.usePartitioning ? 'X' : ' ',
				run.transTimeCell(),
				run.checkTimeCell(),
				run.totalTimeCell(),
				run.completedTranslations.get() + ", " + run.completedChecks.get()
			);
		}
	}
	
	/**
	 * Returns a list of parameters to be tested.
	 * @return
	 */
	@Parameters(name = "{0} ({2}, {3}, {4})")
	public static Collection<Object[]> params() {
		ArrayList<Object[]> params = new ArrayList<>();
		// Comment out the datasets that should not be tested
//		params.addAll(dataHotelVariants());
		params.addAll(dataBigDiagrams());
//		params.addAll(dataUnitScopeDeadlock());
		return params;
	}
	
	/** Replications of the complex hotel variant to test diagrams with many tasks. */
	private static Collection<Object[]> dataBigDiagrams() {
		return Stream.of(
//			"complex x5",
//			"complex x10",
//			"complex x15",
//			"complex x20"//,
//			"complex x25"//,
//			"complex x30",
//			"complex x35"//,
//			"complex x40",
//			"complex x60",
//			"complex x80"//,
//			"complex x100"//,
			"complex x120"//,
//			"complex x150"//,
//			"complex x200"//,
//			"complex x400",
//			"complex x800",
//			"complex x1200",
//			"complex x1600",
//			"complex x2000"
		).map(m -> new Object[] { m, false,  true,  true,  true, 0, 3 })
			.collect(Collectors.toList());
	}
	
	/** Deadlock with alternate precedence at unit scope with increasing number of locations. */
	private static Collection<Object[]> dataUnitScopeDeadlock() {
		return Stream.of(
//			"unit-scope-deadlock-50",
//			"unit-scope-deadlock-100",
//			"unit-scope-deadlock-150",
//			"unit-scope-deadlock-200",
//			"unit-scope-deadlock-300",			
//			"unit-scope-deadlock-400",
//			"unit-scope-deadlock-600",
			"unit-scope-deadlock-800"
		).map(m -> new Object[] { m, false,  true,  true,  true, 0, 3 })
			.collect(Collectors.toList());
	}

	/** Four variants of the hotel examples, to compare strategies. */
	private static Collection<Object[]> dataHotelVariants() {
		List<Object[]> params = new ArrayList<>();
		String[] modelNames = {
			"consistent",
			"cyclic",
			"deadlock",
			"complex"
		};
		boolean[] expectSuccess = {
			true,
			false,
			false,
			false
		};
		int warmup = 100;
		int iterations = 100;
		for (int i = 0; i < modelNames.length; i++) {
			String m = modelNames[i];
			boolean e = expectSuccess[i];
			// Put sophisticated first so the JIT will optimize them less,
			// thus if they still take less time it is not because of the JIT. 
			params.add(new Object[] { m, e,  true,  true,  true, warmup, iterations });
			params.add(new Object[] { m, e, false,  true,  true, warmup, iterations });
			params.add(new Object[] { m, e,  true,  true, false, warmup, iterations });
//			params.add(new Object[] { m, e, false,  true, false, warmup, iterations });
//			params.add(new Object[] { m, e,  true, false,  true, warmup, iterations });
//			params.add(new Object[] { m, e, false, false,  true, warmup, iterations });
//			params.add(new Object[] { m, e,  true, false, false, warmup, iterations });
//			params.add(new Object[] { m, e, false, false, false, warmup, iterations });
		}
		return params;
	}
	
	/** Read the model of the specified name from an MDL file of the same name. */
	@Before
	public void setUp() throws Exception {
		new TestContextManager(getClass()).prepareTestInstance(this);
		MDLFileAST mdl = objectMapper.readValue(TestUtil.load("consistency/" + modelName + ".mdl"), MDLFileAST.class);
		model = MDLContext.create().files().toEntity(mdl);
		warmUp();
	}
	
	/** Execute some warm-up experiments to trigger optimizations before time is measured. */
	private void warmUp() {
		for (int i = 0; i < warmup; i++) {
			graph = modelToGraphTranslator.translate(model.getTasks(), ignoreSimpleEdges);
			orientationFinder.init(true, true).search(graph).isSuccessful();
		}
	}
	
	@Test
	public void test() throws JsonParseException, IOException {
		// Add first to also consider timed-out runs
		allRuns.add(this);
		for (int i = 0; i < iterations; i++) {
			long t0 = System.nanoTime();
			graph = modelToGraphTranslator.translate(model.getTasks(), ignoreSimpleEdges);
			long t1 = System.nanoTime();
			transTimeNs.addAndGet(t1 - t0);
			completedTranslations.incrementAndGet();
//			boolean success = expectSuccess;	// For testing translation only
			boolean success = orientationFinder.init(usePartitioning, useResolving).search(graph).isSuccessful();
			long t2 = System.nanoTime();
			checkTimeNs.addAndGet(t2 - t1);
			completedChecks.incrementAndGet();
			
			if (expectSuccess)
				assertTrue(success);
			else
				assertFalse(success);
		}
	}
	
	private long transTimeNs() {
		return avgTime(transTimeNs.get(), completedTranslations.get());
	}
	
	private long checkTimeNs() {
		return avgTime(checkTimeNs.get(), completedChecks.get());
	}
	
	private String transTimeCell() {
		return timeCell(transTimeNs() / 1000000);
	}
	
	private String checkTimeCell() {
		return timeCell(checkTimeNs() / 1000000);
	}
	
	private String totalTimeCell() {
		long trans = transTimeNs();
		long check = checkTimeNs();
		long total = trans < 0 || check < 0 ? -1 : (trans + check) / 1000000;
		return timeCell(total);
	}
	
	private long avgTime(long sumNs, int iterations) {
		return iterations > 0
			? sumNs / iterations
			: -1;
	}
	
	private String timeCell(long ms) {
		return ms < 0 ? "> " + TIMEOUT_MIN + " min" : String.format("%,d", ms);
	}
}
