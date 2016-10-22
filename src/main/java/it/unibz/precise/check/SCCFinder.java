package it.unibz.precise.check;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface SCCFinder {
	
	<T> List<List<T>> findSCCs(Collection<T> nodes, Function<T, Stream<T>> adj);

}
