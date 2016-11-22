package it.unibz.util;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
	
	public static <T, R> List<R> mapToList(Collection<T> coll, Function<T, R> mapper) {
		return coll == null ? null
			: coll.stream().map(mapper).collect(Collectors.toList());
	}
	
	public static <T, K, V> Map<K, V> mapToMap(Collection<T> coll, Function<T, K> keyMapper, Function<T, V> valMapper) {
		return coll == null ? null
			: coll.stream().collect(Collectors.toMap(keyMapper, valMapper));
	}
	
	public static <T> Map<T, Integer> mapToIndex(List<T> list) {
		return IntStream.range(0, list.size())
			.mapToObj(Integer::valueOf)
			.collect(Collectors.toMap(list::get, Function.identity()));
	}

	public static Collector<Integer, BitSet, BitSet> toBitSetCollector() {
		return Collector.<Integer, BitSet>of(BitSet::new, BitSet::set, (bs1, bs2) -> {
			bs1.or(bs2);
			return bs1;
		});
	}
	
	public static Optional<Integer> parseInteger(String s) {
		try {
			return Optional.of(Integer.valueOf(s));
		} catch (NumberFormatException nfe) {
			return Optional.empty();
		}
	}
	
	public static <T, R> Function<T, R> memoize(Function<T, R> func, boolean concurrent) {
		Map<T, R> cache = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
		return t -> cache.computeIfAbsent(t, func);
	}
	
	public static <T> Set<T> asSet(Collection<T> coll) {
		return coll instanceof Set ? (Set<T>)coll
			: coll == null ? Collections.emptySet()
			: new HashSet<T>(coll);
	}
	
	public static <T> int size(Collection<T> coll) {
		return coll == null ? 0 : coll.size();
	}
	
	public static <T> boolean containSameElements(Collection<T> coll1, Collection<T> coll2) {
		int size1 = size(coll1);
		int size2 = size(coll2);
		return size1 == size2
			&& (size1 == 0 || asSet(coll1).equals(asSet(coll2)));
	}
	
}
