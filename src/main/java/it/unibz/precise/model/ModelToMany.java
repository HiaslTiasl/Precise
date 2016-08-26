package it.unibz.precise.model;
import java.util.List;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.util.CollectionBidirection;

class ModelToMany {
	
	
	static final CollectionBidirection<Model, Phase, List<Phase>> PHASES =
		new CollectionBidirection<>(
			Model::getPhases,
			Model::internalSetPhases,
			Phase::getModel,
			Phase::internalSetModel
		);

	static final CollectionBidirection<Model, Attribute, List<Attribute>> ATTRIBUTES =
		new CollectionBidirection<>(
			Model::getAttributes,
			Model::internalSetAttributes,
			Attribute::getModel,
			Attribute::internalSetModel
		);
	
	static final CollectionBidirection<Model, TaskType, List<TaskType>> TYPES =
		new CollectionBidirection<>(
			Model::getTaskTypes,
			Model::internalSetTaskTypes,
			TaskType::getModel,
			TaskType::internalSetModel
		);

	static final CollectionBidirection<Model, Task, List<Task>> TASKS =
		new CollectionBidirection<>(
			Model::getTasks,
			Model::internalSetTasks,
			Task::getModel,
			Task::internalSetModel
		);
	
	static final CollectionBidirection<Model, Dependency, List<Dependency>> DEPENDENCIES =
		new CollectionBidirection<>(
			Model::getDependencies,
			Model::internalSetDependencies,
			Dependency::getModel,
			Dependency::internalSetModel
		);

}
