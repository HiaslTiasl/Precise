package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.CollectionBidirection;
import it.unibz.util.OneToManyBidirection;

/**
 * {@link OneToManyBidirection}s from {@link Activity} to other classes.
 * 
 * @author MatthiasP
 *
 */
public class ActivityToMany {

	static final CollectionBidirection<Activity, Task, List<Task>> TASKS =
		new CollectionBidirection<>(
			Activity::getTasks,
			Activity::internalSetTasks,
			Task::getActivity,
			Task::internalSetActivity
		);

}
