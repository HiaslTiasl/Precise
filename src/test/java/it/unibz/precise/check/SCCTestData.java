package it.unibz.precise.check;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SCCTestData {
	
	private String name;
	
	private BitSet[] adj;
	
	private Set<BitSet> expectedSCCs;
	
	public SCCTestData(String name, BitSet[] adj, Set<BitSet> expectedSCCs) {
		this.name = name;
		this.adj = adj;
		this.expectedSCCs = expectedSCCs;
	}

	public String getName() {
		return name;
	}

	public BitSet[] getAdj() {
		return adj;
	}

	public Set<BitSet> getExpectedSCCs() {
		return expectedSCCs;
	}
	
	public Object[] toArray() {
		return new Object[] {name, adj, expectedSCCs};
	}
	
	@SafeVarargs
	static <T> Set<T> asSet(T ...values) {
		return Stream.of(values).collect(Collectors.toSet());
	}

	@SafeVarargs
	static BitSet asBitSet(int ...values) {
		BitSet bitSet = new BitSet();
		for (int v : values)
			bitSet.set(v);
		return bitSet;
	}
	
	public static SCCTestData emptyGraph() {
		return new SCCTestData(
			"emptyGraph",
			new BitSet[] { asBitSet() },
			Collections.emptySet()
		);
	}
	
	public static SCCTestData dag() {
		return new SCCTestData(
			"dag",
			new BitSet[] {
				asBitSet(1, 2),
				asBitSet(2, 3),
				asBitSet(3)
			},
			asSet(asBitSet(0), asBitSet(1), asBitSet(2), asBitSet(3))
		);
	}
	
	public static SCCTestData twoCycles() {
		return new SCCTestData(
			"twoCycles",
			new BitSet[] {
				asBitSet(1),
				asBitSet(2),
				asBitSet(0, 3),
				asBitSet(4),
				asBitSet(5),
				asBitSet(4)
			},
			asSet(asBitSet(0, 1, 2), asBitSet(3), asBitSet(4, 5))
		);
	}
	
	public static SCCTestData multiplyCyclesPerSCC() {
		return new SCCTestData(
			"multiplyCyclesPerSCC",
			new BitSet[] {
				asBitSet(1, 2),
				asBitSet(2, 3),
				asBitSet(3),
				asBitSet(0)
			},
			asSet(asBitSet(0, 1, 2, 3))
		);
	}

}
