package it.unibz.precise.model;

import java.util.List;

import it.unibz.util.CollectionBidirection;

public class TaskTypeToMany {

	static final CollectionBidirection<TaskType, Task, List<Task>> TASKS =
		new CollectionBidirection<>(
			TaskType::getTasks,
			TaskType::internalSetTasks,
			Task::getType,
			Task::internalSetType
		);

}
