define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypeCreateDialogController.$inject = ['TaskTypes'];
	
	function SingleModelTaskTypeCreateDialogController(TaskTypes) {
		
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
