define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypeCreateDialogController.$inject = ['TaskTypes', 'errorHandler'];
	
	function SingleModelTaskTypeCreateDialogController(TaskTypes, errorHandler) {
		
		var $ctrl = this;
		
		$ctrl.sendTaskType = sendTaskType;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
			$ctrl.crafts = $ctrl.resolve.crafts;
			$ctrl.phaseFixed = !!_.get($ctrl.resource.data, 'phase');
		}
		
		function sendTaskType() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close, errorHandler.handle);
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
