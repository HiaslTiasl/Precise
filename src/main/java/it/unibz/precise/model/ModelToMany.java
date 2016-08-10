package it.unibz.precise.model;
import java.util.List;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.util.OneToManyBidirection;

class ModelToMany {
	
	
	static final OneToManyBidirection<Model, Phase, List<Phase>> PHASES =
		new OneToManyBidirection<>(
			Model::getPhases,
			Model::internalSetPhases,
			Phase::getModel,
			Phase::internalSetModel
		);

	static final OneToManyBidirection<Model, Attribute, List<Attribute>> ATTRIBUTES =
		new OneToManyBidirection<>(
			Model::getAttributes,
			Model::internalSetAttributes,
			Attribute::getModel,
			Attribute::internalSetModel
		);
	
	static final OneToManyBidirection<Model, TaskType, List<TaskType>> TYPES =
		new OneToManyBidirection<>(
			Model::getTaskTypes,
			Model::internalSetTaskTypes,
			TaskType::getModel,
			TaskType::internalSetModel
		);

	static final OneToManyBidirection<Model, Task, List<Task>> TASKS =
		new OneToManyBidirection<>(
			Model::getTasks,
			Model::internalSetTasks,
			Task::getModel,
			Task::internalSetModel
		);
	
	static final OneToManyBidirection<Model, Dependency, List<Dependency>> DEPENDENCIES =
		new OneToManyBidirection<>(
			Model::getDependencies,
			Model::internalSetDependencies,
			Dependency::getModel,
			Dependency::internalSetModel
		);

}
