/**
 * Angular component for viewing and setting task properties in a dialog.
 * @module "singleModel/singleModel-diagram.create-task-dialog.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelDiagramCreateTaskDialogController.$inject = ['$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'Tasks', 'TaskTypes', 'Phases'];
	
	/**
	 * Controller constructor.
	 * @constructor
	 */
	function SingleModelDiagramCreateTaskDialogController($uibModal, errorHandler, PreciseApi, Pages, Tasks, TaskTypes, Phases) {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.taskDefinitionChanged = taskDefinitionChanged;
		$ctrl.createTaskDefinition = createTaskDefinition;
		$ctrl.computePitches = computePitches;
		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
			resetPhase();
		}
		
		/** Show the given error regarding pitch parameters. */
		function setPitchError(pitchError) {
			$ctrl.pitchError = pitchError;
		}
		
		/** 
		 * Ask the server to compute missing pitch parameters and resulting
		 * man-hours, or to check whether the given parameters are consistent.
		 */
		function computePitches() {
			$ctrl.resource.computePitches()
				.then(_.constant(null), PreciseApi.getErrorText)
				.then(setPitchError);
		}
		
		/** Restrict the available task definitions to the given list. */
		function setTaskDefinitions(taskTypes) {
			$ctrl.taskTypes = taskTypes;
		}
		
		/** The current phase changed, so update the available task definitions. */
		function phaseChanged() {
			if ($ctrl.phase)
				setPhase();
			else
				resetPhase();
		}
		
		/** The phase was reset, so let the user select all task definitions of the model. */
		function resetPhase() {
			loadTaskTypesFrom($ctrl.resource.model);
		}
		
		/** A phase was set, so restrict the available task definitions to those of that phase. */
		function setPhase() {
			Phases.existingResource($ctrl.resource.model, $ctrl.phase)
				.then(loadTaskTypesFrom);			
		}
		
		/**
		 * Loads the list of task types associated to the given resource,
		 * which can be either a model or a phase.
		 */
		function loadTaskTypesFrom(resource) {
			// Reset old list of task types first so they cannot be selected.
			setTaskDefinitions(null);
			resource.getTaskTypes({
				projection: TaskTypes.Resource.prototype.defaultProjection
			})
			.then(Pages.collectRemaining)
			.then(setTaskDefinitions);			
		}

		/** The task definition changed, so update the phase accordingly. */
		function taskDefinitionChanged() {
			if ($ctrl.phase != $ctrl.resource.data.type.phase) 
				$ctrl.phase = $ctrl.resource.data.type.phase;
		}
		
		/** Returns a promise of the crafts of the model, and caches the result. */
		var getCrafts = _.once(function () {
			return $ctrl.resource.model.getCrafts();
		});
		
		/** Opens a dialog for creating a task definition that is to be used in this task. */
		function createTaskDefinition() {
			$uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					resource: function () {
						return TaskTypes.newResource($ctrl.resource.model, {
							phase: $ctrl.phase
						});
					},
					phases: _.constant($ctrl.phases),	// Restrict the available phases
					crafts: getCrafts
				}
			}).result.then(function (result) {
				$ctrl.taskTypes.push(result);
				$ctrl.resource.data.type = result;
				taskDefinitionChanged();
			}, errorHandler.handle);
		}
		
		/** Send the task resource to apply the changes on the server and close the dialog. */
		function send() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close);
		}
		
		/** Cancel editing and dismiss the dialog. */
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
