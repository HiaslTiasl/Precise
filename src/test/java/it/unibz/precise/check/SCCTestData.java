package it.unibz.precise.check;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SCCTestData {
	
	private String name;
	
	private List<List<Integer>> adj;
	
	private Set<Set<Integer>> expectedSCCs;
	
	public SCCTestData(String name, List<List<Integer>> adj, Set<Set<Integer>> expectedSCCs) {
		this.name = name;
		this.adj = adj;
		this.expectedSCCs = expectedSCCs;
	}

	public String getName() {
		return name;
	}

	public List<List<Integer>> getAdj() {
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
			Collections.emptyList(),
			Collections.emptySet()
		);
	}
	
	public static SCCTestData dag() {
		return new SCCTestData(
			"dag",
			Arrays.asList(
				Arrays.asList(1, 2),
				Arrays.asList(2, 3),
				Arrays.asList(3),
				Arrays.asList()
			),
			asSet(asSet(0), asSet(1), asSet(2), asSet(3))
		);
	}
	
	public static SCCTestData twoCycles() {
		return new SCCTestData(
			"twoCycles",
			Arrays.asList(
				Arrays.asList(1),
				Arrays.asList(2),
				Arrays.asList(0, 3),
				Arrays.asList(4),
				Arrays.asList(5),
				Arrays.asList(4)
			),
			asSet(
				asSet(0, 1, 2),
				asSet(3),
				asSet(4, 5)
			)
		);
	}
	
	public static SCCTestData multiplyCyclesPerSCC() {
		return new SCCTestData(
			"multiplyCyclesPerSCC",
			Arrays.asList(
				Arrays.asList(1, 2),
				Arrays.asList(2, 3),
				Arrays.asList(3),
				Arrays.asList(0)
			),
			asSet(asSet(0, 1, 2, 3))
		);
	}

}
