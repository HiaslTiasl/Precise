package it.unibz.precise.check;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.unibz.precise.Application;
import it.unibz.precise.TestUtil;
import it.unibz.precise.graph.disj.AcyclicOrientationFinder;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.conversion.MDLContext;

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

@RunWith(Parameterized.class)
@SpringBootTest(classes=Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
public class ConsistencyCheckerTest {
	
	private static final int ITERATIONS = 5;
	private static final int WARMUP_ITERATIONS = 0;
	private static final int TIMEOUT_MIN = 30;
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
	
	private AtomicLong transTimeNs = new AtomicLong();
	private AtomicLong checkTimeNs = new AtomicLong();
	
	private AtomicInteger completedTranslations = new AtomicInteger();
	private AtomicInteger completedChecks = new AtomicInteger();
	
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
				run.graph.arcs().size(),
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
	
	@Parameters(name = "{0} ({2}, {3}, {4})")
	public static Collection<Object[]> params() {
		return dataBigDiagrams();
	}
	
	private static Collection<Object[]> dataBigDiagrams() {
		return Stream.of(
//			"complex x5",
//			"complex x10",
//			"complex x15",
//			"complex x20",
//			"complex x25",
			"complex x30"//,
//			"complex x40",
//			"complex x60",
//			"complex x80",
//			"complex x100",
//			"complex x100",
//			"complex x200",
//			"complex x400",
//			"complex x800",
//			"complex x1200",
//			"complex x1600",
//			"complex x2000"
		).map(m -> new Object[] { m, false,  true,  true,  true })
			.collect(Collectors.toList());
	}
	
	private static Collection<Object[]> dataUnitScopeDeadlock() {
		return Stream.of(
			"unit-scope-deadlock-50",
			"unit-scope-deadlock-100",
			"unit-scope-deadlock-150",
			"unit-scope-deadlock-200",
			"unit-scope-deadlock-400",
			"unit-scope-deadlock-800"
		).map(m -> new Object[] { m, false,  true,  true,  true })
			.collect(Collectors.toList());
	}

	private static Collection<Object[]> dataHotelVariants() {
		List<Object[]> params = new ArrayList<>();
		String[] modelNames = { "consistent", "cyclic", "deadlock", "complex" };
		boolean[] expectSuccess = { true, false, false, false };
		
		for (int i = 0; i < modelNames.length; i++) {
			String m = modelNames[i];
			boolean e = expectSuccess[i];
			// Put sophisticated first so the JIT will optimize them less,
			// thus if they still take less time it is not because of the JIT. 
//			params.add(new Object[] { m, e,  true,  true,  true });
//			params.add(new Object[] { m, e, false,  true,  true });
			params.add(new Object[] { m, e,  true,  true, false });
//			params.add(new Object[] { m, e, false,  true, false });
			params.add(new Object[] { m, e,  true, false,  true });
//			params.add(new Object[] { m, e, false, false,  true });
//			params.add(new Object[] { m, e,  true, false, false });
//			params.add(new Object[] { m, e, false, false, false });
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
			transTimeNs.addAndGet(t1 - t0);
			completedTranslations.incrementAndGet();
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
	
	private String transTimeCell() {
		return timeCell(avgTimeMs(transTimeNs.get(), completedTranslations.get()));
	}
	
	private String checkTimeCell() {
		return timeCell(avgTimeMs(checkTimeNs.get(), completedChecks.get()));
	}
	
	private String totalTimeCell() {
		return timeCell(avgTimeMs(transTimeNs.get() + checkTimeNs.get(), completedChecks.get()));
	}
	
	private long avgTimeMs(long sumNs, int iterations) {
		return completedChecks.get() > 0
			? sumNs / iterations / 1000000
			: -1;
	}
	
	private String timeCell(long ms) {
		return ms < 0 ? "> " + TIMEOUT_MIN + " min" : String.format("%,d", ms);
	}
}
