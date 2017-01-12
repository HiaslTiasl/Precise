package it.unibz.precise.check;

import java.util.Comparator;

public interface ConsistencyClassification {

	public enum Category {
		COMPLETENESS,
		SATISFIABILITY
	}

	public static Comparator<ConsistencyClassification> BY_CATEGORY_AND_TYPE =
		Comparator.comparing(ConsistencyClassification::getCategory)
			.thenComparing(ConsistencyClassification::getType);

	Category getCategory();
	
	String getType();
	
}
