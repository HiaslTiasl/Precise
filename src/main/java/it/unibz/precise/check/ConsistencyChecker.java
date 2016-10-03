package it.unibz.precise.check;

import java.util.List;

import it.unibz.precise.model.Model;

public interface ConsistencyChecker {
	
	List<ConsistencyWarning> check(Model model);

}
