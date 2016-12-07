define([
	
], function (
	
) {
	'use strict';
	
	ErrorDialogComponentController.$inject = [];
	
	function ErrorDialogComponentController() {
		
		var $ctrl = this;
		
		$ctrl.close = close;
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.errors = $ctrl.resolve.errors;
		}
		
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