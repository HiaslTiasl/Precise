package it.unibz.precise.check;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import it.unibz.precise.check.ModelProblem.TaskLocation;
import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Model;

/**
 * Checks a process model for problems based on criteria described
 * by the implemented {@code ConsistencyClassification}.
 * All {@link ModelProblem}s emitted by a {@code ProblemChecker}
 * must be of the same classification (see {@link #warning(String, List, List) warning()}).
 * Also, classification of a {@code ProblemChecker} must not change over time.
 * 
 * The implementing classes should be annotated as {@link Service}.
 * 
 * @author MatthiasP
 *
 */
public interface ProblemChecker extends ProblemClassification {
	
	@Override
	Category getCategory();
	
	@Override
	String getType();
	
	/** Check the given model and return any {@link ModelProblem}s found. */
	Stream<ModelProblem> check(Model model);
	
	/** Create a {@link ModelProblem} of this checker's category and type. */
	default <E extends BaseEntity> ModelProblem warning(String msg, Collection<E> entities, Collection<TaskLocation> locations) {
		return new ModelProblem(getCategory(), getType(), msg, entities, locations);
	}

}
