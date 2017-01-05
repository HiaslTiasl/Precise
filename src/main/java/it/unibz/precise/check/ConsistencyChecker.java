package it.unibz.precise.check;

import it.unibz.precise.check.ConsistencyWarning.Category;
import it.unibz.precise.check.ConsistencyWarning.TaskLocation;
import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Model;

import java.util.List;
import java.util.stream.Stream;

public interface ConsistencyChecker {
	
	Category getCategory();
	
	String getType();
	
	Stream<ConsistencyWarning> check(Model model);
	
	/** Create a {@link ConsistencyWarning} of this checker's category and type. */
	default <E extends BaseEntity> ConsistencyWarning warning(String msg, List<E> entities, List<TaskLocation> locations) {
		return new ConsistencyWarning(getCategory(), getType(), msg, entities, locations);
	}

}
