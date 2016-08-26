package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.CollectionBidirection;

public class TaskToMany {

	static final CollectionBidirection<Task, Location, List<Location>> LOCATIONS =
		new CollectionBidirection<>(
			Task::getLocations,
			Task::internalSetLocations,
			Location::getTask,
			Location::internalSetTask
		);
	
	static final CollectionBidirection<Task, OrderSpecification, List<OrderSpecification>> ORDER_SPECIFICATIONS =
		new CollectionBidirection<>(
			Task::getOrderSpecifications,
			Task::internalSetOrderSpecifications,
			OrderSpecification::getTask,
			OrderSpecification::internalSetTask
		);

}
