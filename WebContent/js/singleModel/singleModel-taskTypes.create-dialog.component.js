define([], function () {
	'use strict';
	
	
	function SingleModelTaskTypeCreateDialogController() {
		
		var $ctrl = this;
		
		$ctrl.taskType = {};
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.phases = $ctrl.resolve.phases;
		}
		
		function createTaskType() {
			$ctrl.modalInstance.close($ctrl.taskType);
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
