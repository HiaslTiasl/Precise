define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelDiagramCreateTaskDialogController.$inject = ['$uibModal', 'Pages', 'Tasks', 'TaskTypes', 'Phases'];
	
	function SingleModelDiagramCreateTaskDialogController($uibModal, Pages, Tasks, TaskTypes, Phases) {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.taskDefinitionChanged = taskDefinitionChanged;
		$ctrl.createTaskDefinition = createTaskDefinition;
		$ctrl.createTask = createTask;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
			resetPhase();
		}
		
		function setTaskDefinitions(taskTypes) {
			$ctrl.taskTypes = taskTypes;
		}
		
		function phaseChanged() {
			if ($ctrl.phase)
				setPhase();
			else
				resetPhase();
		}
		
		function resetPhase() {
			loadTaskTypesFrom($ctrl.resource.model);
		}
		
		function setPhase() {
			Phases.existingResource($ctrl.resource.model, $ctrl.phase)
				.then(loadTaskTypesFrom);			
		}
		
		function loadTaskTypesFrom(resource) {
			// Reset old list of task types first so they cannot be selected.
			setTaskDefinitions(null);
			resource.getTaskTypes({
				projection: TaskTypes.Resource.prototype.defaultProjection
			})
			.then(Pages.collectRemaining)
			.then(setTaskDefinitions);			
		}
		
		function taskDefinitionChanged() {
			if ($ctrl.phase != $ctrl.resource.data.type.phase) 
				$ctrl.phase = $ctrl.resource.data.type.phase;
			if (!$ctrl.resource.data.type.unitOfMeasure)
				$ctrl.resource.data.durationType = 'MANUAL';
		}
		
		var getCrafts = _.once(function () {
			return $ctrl.resource.model.getCrafts();
		});
		
		function createTaskDefinition() {
			$uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					resource: function () {
						return TaskTypes.newResource($ctrl.resource.model, {
							phase: $ctrl.phase
						});
					},
					phases: _.constant($ctrl.phases),
					crafts: getCrafts
				}
			}).result.then(function (result) {
				$ctrl.taskTypes.push(result);
				$ctrl.resource.data.type = result;
				taskDefinitionChanged();
			});
		}
		
		function createTask() {
			$ctrl.resource.create()
				.then($ctrl.modalInstance.close);
		}
		
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-diagram.create-task-dialog.html',
		controller: SingleModelDiagramCreateTaskDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
