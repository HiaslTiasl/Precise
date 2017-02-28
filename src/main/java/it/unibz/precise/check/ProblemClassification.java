package it.unibz.precise.check;

import java.util.Comparator;

/**
 * Classifies {@link ModelProblem}s in terms of a category and a type.
 * 
 * @author MatthiasP
 *
 */
public interface ProblemClassification {

	/** A broad categorization of warnings. */
	public enum Category {
		STRUCTURE_WARNING,
		STRUCTURE_ERROR,
		CONSISTENCY_ERROR
	}

	/** Compares {@code ConsistencyClassification}s first by category and then by type. */
	public static Comparator<ProblemClassification> BY_CATEGORY_AND_TYPE =
		Comparator.comparing(ProblemClassification::getCategory)
			.thenComparing(ProblemClassification::getType);

	/** A broad categorization of warnings. */
	Category getCategory();

	/** A name of a kind of warnings, e.g. {@code "cycles"}. */
	String getType();
	
}
