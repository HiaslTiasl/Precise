package it.unibz.precise.check;

import java.util.Comparator;

/**
 * Classifies ConsistencyWarnings in terms of a category and a type.
 * 
 * @author MatthiasP
 *
 */
public interface ConsistencyClassification {

	/** A broad categorization of warnings. */
	public enum Category {
		COMPLETENESS,
		SATISFIABILITY
	}

	/** Compares {@code ConsistencyClassification}s first by category and then by type. */
	public static Comparator<ConsistencyClassification> BY_CATEGORY_AND_TYPE =
		Comparator.comparing(ConsistencyClassification::getCategory)
			.thenComparing(ConsistencyClassification::getType);

	/** A broad categorization of warnings, e.g. {@link Category#SATISFIABILITY}. */
	Category getCategory();

	/** A name of a kind of warnings, e.g. {@code "cycles"}. */
	String getType();
	
}
