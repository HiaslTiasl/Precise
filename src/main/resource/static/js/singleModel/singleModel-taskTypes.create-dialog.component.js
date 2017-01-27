/**
 * Angular component for viewing and setting task definition properties in a dialog.
 * @module "singleModel/singleModel-taskTypes.create-dialog.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypeCreateDialogController.$inject = ['TaskTypes', 'errorHandler'];
	
	/**
	 * Controller constructor.
	 * @constructor
	 */
	function SingleModelTaskTypeCreateDialogController(TaskTypes, errorHandler) {
		
		var $ctrl = this;
		
		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
			$ctrl.crafts = $ctrl.resolve.crafts;
			// If a phase is passed, it is fixed such that it cannot be changed
			$ctrl.phaseFixed = !!_.get($ctrl.resource.data, 'phase');
		}
		
		/** Sends the resource to the server to apply the changes and close the dialog. */
		function send() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close, errorHandler.handle);
		}
		
		/** Cancels editing and dismiss the dialog. */
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
