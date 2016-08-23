package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.OneToManyBidirection;

public class TaskToMany {

	static final OneToManyBidirection<Task, Location, List<Location>> LOCATIONS =
		new OneToManyBidirection<>(
			Task::getLocations,
			Task::internalSetLocations,
			Location::getTask,
			Location::internalSetTask
		);
	
	static final OneToManyBidirection<Task, OrderSpecification, List<OrderSpecification>> ORDER_SPECIFICATIONS =
		new OneToManyBidirection<>(
			Task::getOrderSpecifications,
			Task::internalSetOrderSpecifications,
			OrderSpecification::getTask,
			OrderSpecification::internalSetTask
		);

}
