package it.unibz.util;

import java.util.BitSet;
import java.util.stream.Collector;

public class Util {

	public static Collector<Integer, BitSet, BitSet> toBitSetCollector() {
		return Collector.<Integer, BitSet>of(BitSet::new, BitSet::set, (bs1, bs2) -> {
			bs1.or(bs2);
			return bs1;
		});
	}
	
}
