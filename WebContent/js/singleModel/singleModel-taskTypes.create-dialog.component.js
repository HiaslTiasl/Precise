define([], function () {
	'use strict';
	
	SingleModelTaskTypeCreateDialogController.$inject = ['TaskTypes'];
	
	function SingleModelTaskTypeCreateDialogController(TaskTypes) {
		
		var $ctrl = this;
		
		$ctrl.taskType = {};
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.phases = $ctrl.resolve.phases;
			$ctrl.crafts = $ctrl.resolve.crafts;
		}
		
		function createTaskType() {
			TaskTypes
				.newResource($ctrl.resolve.model, $ctrl.taskType)
				.then(function (resource) {
					return resource.create();
				})
				.then($ctrl.modalInstance.close);
		}
		
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-taskTypes.create-dialog.html',
		controller: SingleModelTaskTypeCreateDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
