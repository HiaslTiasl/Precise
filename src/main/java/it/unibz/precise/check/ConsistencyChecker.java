package it.unibz.precise.check;

import java.util.stream.Stream;

import it.unibz.precise.model.Model;

public interface ConsistencyChecker {
	
	Stream<ConsistencyWarning> check(Model model);

}
