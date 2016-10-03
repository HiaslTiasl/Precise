package it.unibz.precise.check;

import java.util.List;

public interface SCCFinder {
	
	List<List<Integer>> findSCCs(List<List<Integer>> adj);

}
