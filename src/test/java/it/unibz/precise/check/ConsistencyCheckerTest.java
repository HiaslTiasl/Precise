package it.unibz.precise.check;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import it.unibz.precise.Application;
import it.unibz.precise.graph.disj.AcyclicOrientationFinder;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.graph.disj.OrientationResult;
import it.unibz.precise.model.Model;
import it.unibz.precise.rep.ModelRepository;

@SpringBootTest(classes=Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
@Transactional
public class ConsistencyCheckerTest {
	
	
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ModelToGraphTranslator modelToGraphTranslator;
	
	@Autowired
	private AcyclicOrientationFinder orientationFinder;
	
	private Model consistentModel;
	private Model cyclicModel;
	private Model deadlockModel;
	
	private boolean modelsInitialized;

	@Before
	public void checkSetUp() {
		if (!modelsInitialized) {
			modelsInitialized = true;
			consistentModel = repository.findByName("hotel");
			cyclicModel     = repository.findByName("hotel - cyclic");
			deadlockModel   = repository.findByName("hotel - deadlock");
		}
	}
	
	private String literal(String name, boolean value) {
		return (value ? ' ' : '!') + name ;
	}
	
	private boolean test(Model model, boolean ignoreSimpleEdges, boolean partitioning, boolean resolving) {
		int iterations = 1000;
		boolean success = true;
		long sumTranslate = 0;
		long sumCheck = 0;
		for (int i = 0; i < iterations + 1; i++) {
			long t0 = System.nanoTime();
			DisjunctiveGraph<TaskUnitNode> graph = modelToGraphTranslator.translate(model.getTasks(), ignoreSimpleEdges);
			long t1 = System.nanoTime();
			OrientationResult<TaskUnitNode> res =  orientationFinder.init(partitioning, resolving).search(graph);
			long t2 = System.nanoTime();
			if (i > 0) {
				// First iteration is warm-up
				success = success && res.isSuccessful(); 
				sumTranslate += t1 - t0;
				sumCheck += t2 - t1;
			}
		}
		long avgTranslate = sumTranslate / iterations;
		long avgCheck = sumCheck / iterations;
		System.out.printf("test(%s, %s, %s, %s): % ,12d + % ,12d = % ,12d ns\n",
			model.getName(),
			literal("ignoreSimpleEdges", ignoreSimpleEdges),
			literal("partitioning", partitioning),
			literal("resolving", resolving),
			avgTranslate, avgCheck, avgTranslate + avgCheck
		);
		return success;
	}

	@Test
	public void testConsistent() {
		assertTrue(test(consistentModel, false, false, false));
	}
	
	@Test
	public void testConsistentIgnoreSimple() {
		assertTrue(test(consistentModel, true, false, false));
	}
	
	@Test
	public void testConsistentPartitioning() {
		assertTrue(test(consistentModel, false, true, false));
	}
	
	@Test
	public void testConsistentResolving() {
		assertTrue(test(consistentModel, false, false, true));
	}
	
	@Test
	public void testConsistentIgnoreSimplePartitioning() {
		assertTrue(test(consistentModel, true, true, false));
	}
	
	@Test
	public void testConsistentIgnoreSimpleResolving() {
		assertTrue(test(consistentModel, true, false, true));
	}
	
	@Test
	public void testConsistentPartitioningResolving() {
		assertTrue(test(consistentModel, false, true, true));
	}
	
	@Test
	public void testConsistentIgnoreSimplePartitioningResolving() {
		assertTrue(test(consistentModel, true, true, true));
	}
	
	
	@Test
	public void testCyclic() {
		assertFalse(test(cyclicModel, false, false, false));
	}
	
	@Test
	public void testCyclicIgnoreSimple() {
		assertFalse(test(cyclicModel, true, false, false));
	}
	
	@Test
	public void testCyclicPartitioning() {
		assertFalse(test(cyclicModel, false, true, false));
	}
	
	@Test
	public void testCyclicResolving() {
		assertFalse(test(cyclicModel, false, false, true));
	}
	
	@Test
	public void testCyclicIgnoreSimplePartitioning() {
		assertFalse(test(cyclicModel, true, true, false));
	}
	
	@Test
	public void testCyclicIgnoreSimpleResolving() {
		assertFalse(test(cyclicModel, true, false, true));
	}
	
	@Test
	public void testCyclicPartitioningResolving() {
		assertFalse(test(cyclicModel, false, true, true));
	}
	
	@Test
	public void testCyclicIgnoreSimplePartitioningResolving() {
		assertFalse(test(cyclicModel, true, true, true));
	}
	
	
	
	@Test
	public void testDeadlock() {
		assertFalse(test(deadlockModel, false, false, false));
	}
	
	@Test
	public void testDeadlockIgnoreSimple() {
		assertFalse(test(deadlockModel, true, false, false));
	}
	
	@Test
	public void testDeadlockPartitioning() {
		assertFalse(test(deadlockModel, false, true, false));
	}
	
	@Test
	public void testDeadlockResolving() {
		assertFalse(test(deadlockModel, false, false, true));
	}
	
	@Test
	public void testDeadlockIgnoreSimplePartitioning() {
		assertFalse(test(deadlockModel, true, true, false));
	}
	
	@Test
	public void testDeadlockIgnoreSimpleResolving() {
		assertFalse(test(deadlockModel, true, false, true));
	}
	
	@Test
	public void testDeadlockPartitioningResolving() {
		assertFalse(test(deadlockModel, false, true, true));
	}
	
	@Test
	public void testDeadlockIgnoreSimplePartitioningResolving() {
		assertFalse(test(deadlockModel, true, true, true));
	}
	
	
}
