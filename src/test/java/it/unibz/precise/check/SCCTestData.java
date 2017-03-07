package it.unibz.precise.check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SCCTestData {
	
	private String name;
	
	private Map<Integer, Set<Integer>> adj;
	
	private Set<Set<Integer>> expectedSCCs;
	
	public SCCTestData(String name, Map<Integer, Set<Integer>> adj, Set<Set<Integer>> expectedSCCs) {
		this.name = name;
		this.adj = adj;
		this.expectedSCCs = expectedSCCs;
	}

	public String getName() {
		return name;
	}

	public Map<Integer, Set<Integer>> getAdj() {
		return adj;
	}

	public Set<Set<Integer>> getExpectedSCCs() {
		return expectedSCCs;
	}
	
	public Object[] toArray() {
		return new Object[] {name, adj, expectedSCCs};
	}
	
	@SafeVarargs
	static <T> Set<T> asSet(T ...values) {
		return Stream.of(values).collect(Collectors.toSet());
	}
	
	public static SCCTestData emptyGraph() {
		return new SCCTestData(
			"emptyGraph",
			Collections.emptyMap(),
			Collections.emptySet()
		);
	}
	
	public static SCCTestData dag() {
		Map<Integer, Set<Integer>> adj = new HashMap<>();
		adj.put(0, asSet(1, 2));
		adj.put(1, asSet(2, 3));
		adj.put(2, asSet(3));
		adj.put(3, asSet());
		
		return new SCCTestData(
			"dag",
			adj,
			asSet(asSet(0), asSet(1), asSet(2), asSet(3))
		);
	}
	
	public static SCCTestData twoCycles() {
		Map<Integer, Set<Integer>> adj = new HashMap<>();
		
		adj.put(0, asSet(1));
		adj.put(1, asSet(2));
		adj.put(2, asSet(0, 3));
		adj.put(3, asSet(4));
		adj.put(4, asSet(5));
		adj.put(5, asSet(4));
		
		return new SCCTestData(
			"twoCycles",
			adj,
			asSet(asSet(0, 1, 2), asSet(3), asSet(4, 5))
		);
	}
	
	public static SCCTestData multiplyCyclesPerSCC() {
		Map<Integer, Set<Integer>> adj = new HashMap<>();
		adj.put(0, asSet(1, 2));
		adj.put(1, asSet(2, 3));
		adj.put(2, asSet(3));
		adj.put(3, asSet(0));
		return new SCCTestData(
			"multiplyCyclesPerSCC",
			adj,
			asSet(asSet(0, 1, 2, 3))
		);
	}

}
