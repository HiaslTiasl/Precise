package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.CollectionBidirection;
import it.unibz.util.OneToManyBidirection;

/**
 * {@link OneToManyBidirection}s from {@link Task} to other classes.
 * 
 * @author MatthiasP
 *
 */
public class TaskToMany {

	static final CollectionBidirection<Task, Dependency, List<Dependency>> IN_DEPENDENCIES =
		new CollectionBidirection<>(
			Task::getIn,
			Task::internalSetIn,
			Dependency::getTarget,
			Dependency::internalSetTarget
		);
	
	static final CollectionBidirection<Task, Dependency, List<Dependency>> OUT_DEPENDENCIES =
		new CollectionBidirection<>(
			Task::getOut,
			Task::internalSetOut,
			Dependency::getSource,
			Dependency::internalSetSource
		);

}
