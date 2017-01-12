define([], function () {
	'use strict';
	
	AllModelsCreateDialogController.$inject = ['Models', 'errorHandler'];
	
	function AllModelsCreateDialogController(Models, errorHandler) {
		
		var $ctrl = this;
		
		$ctrl.sendModel = sendModel;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource
		}
		
		function sendModel() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close, errorHandler.handle);
		}
		
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/allModels/allModels.create-dialog.html',
		controller: AllModelsCreateDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
