define(function () {
	'use strict';
	
	ErrorDialogComponentController.$inject = [];
	
	function ErrorDialogComponentController() {
		
		var $ctrl = this;
		
		$ctrl.close = close;
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.errors = $ctrl.resolve.errors;
			if (!Array.isArray($ctrl.errors)) {
				$ctrl.title = errors.title;
				$ctrl.errors = [$ctrl.errors];
			}
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