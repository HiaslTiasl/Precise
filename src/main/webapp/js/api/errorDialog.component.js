/**
 * Component for displaying multiple errors in a modal dialog.
 * @module "api/errorDialog.component"
 */
define(function () {
	'use strict';
	
	ErrorDialogComponentController.$inject = [];
	
	/** 
	 * Component controller.
	 * @constructor
	 */
	function ErrorDialogComponentController() {
		
		var $ctrl = this;
		
		$ctrl.close = close;
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.error = $ctrl.resolve.error;
			if (Array.isArray($ctrl.error.data))
				$ctrl.errData = $ctrl.error.data;
			else {
				$ctrl.errData = [$ctrl.errors.data];
				$ctrl.title = errors.title;
			}
		}
		
		/** Close the dialog. */
		function close() {
			$ctrl.modalInstance.close();
		}
	}
	
	return {
		templateUrl: 'js/api/errorDialog.html',
		controller: ErrorDialogComponentController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
	
});