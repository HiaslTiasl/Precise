package it.unibz.precise.check;

import it.unibz.precise.check.ConsistencyWarning.TaskLocation;
import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Model;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

/**
 * Checks a process model for consistency based on criteria described
 * by the implemented {@code ConsistencyClassification}.
 * All {@link ConsistencyWarning}s emitted by a {@code ConsistencyChecker}
 * must be of the same classification (see {@link #warning(String, List, List) warning()}).
 * Also, classification of a {@code ConsistencyChecker} must not change over time.
 * 
 * The implementing classes should be annotated as {@link Service}.
 * 
 * @author MatthiasP
 *
 */
public interface ConsistencyChecker extends ConsistencyClassification {
	
	@Override
	Category getCategory();
	
	@Override
	String getType();
	
	/** Check the given model and return any {@link ConsistencyWarning}s found. */
	Stream<ConsistencyWarning> check(Model model);
	
	/** Create a {@link ConsistencyWarning} of this checker's category and type. */
	default <E extends BaseEntity> ConsistencyWarning warning(String msg, List<E> entities, List<TaskLocation> locations) {
		return new ConsistencyWarning(getCategory(), getType(), msg, entities, locations);
	}

}
