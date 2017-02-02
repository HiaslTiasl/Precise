package it.unibz.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility functions.
 * 
 * @author MatthiasP
 *
 */
public class Util {
	
	private Util() {
	}
	
	/** Maps the given collection to a list using the given mapper function, or to null if the colleciton is null. */
	public static <T, R> List<R> mapToList(Collection<T> coll, Function<T, R> mapper) {
		return coll == null ? null
			: coll.stream().map(mapper).collect(Collectors.toList());
	}
	
	/** Returns a set containing all elements in the given collection, intended for read-only usage. */
	public static <T> Set<T> asSet(Collection<T> coll) {
		return coll instanceof Set ? (Set<T>)coll						// Return coll itself if already a set
			: coll == null || coll.isEmpty() ? Collections.emptySet()	// Non-modifiable empty set if null or empty
			: new HashSet<T>(coll);										// New set with same elements
	}
	
	/** Returns the size of the given collection, or 0 of the collection is null. */
	public static <T> int size(Collection<T> coll) {
		return coll == null ? 0 : coll.size();
	}
	
	/** Indicates whether the two given collections contain the same elements. */
	public static <T> boolean containSameElements(Collection<T> coll1, Collection<T> coll2) {
		int size1 = size(coll1);
		int size2 = size(coll2);
		return size1 == size2
			&& (size1 == 0 || asSet(coll1).equals(asSet(coll2)));
	}
	
}
