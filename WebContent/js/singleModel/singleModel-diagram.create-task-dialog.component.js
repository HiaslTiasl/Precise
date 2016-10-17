define([], function () {
	'use strict';
	
	SingleModelDiagramCreateTaskDialogController.$inject = ['Pages', 'Tasks', 'TaskTypes', 'Phases'];
	
	function SingleModelDiagramCreateTaskDialogController(Pages, Tasks, TaskTypes, Phases) {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.createTask = createTask;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
		}
		
		function phaseChanged() {
			// Reset old list of task types first so they cannot be selected.
			$ctrl.taskTypes = null;
			Phases.existingResource($ctrl.model, $ctrl.phase)
				.then(function (resource) {
					return resource.getTaskTypes({
						projection: TaskTypes.Resource.prototype.defaultProjection
					});
				})
				.then(Pages.collectRemaining)
				.then(function (taskTypes) {
					$ctrl.taskTypes = taskTypes;
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
