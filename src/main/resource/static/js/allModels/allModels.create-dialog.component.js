define([], function () {
	'use strict';
	
	AllModelsCreateDialogController.$inject = ['Models'];
	
	function AllModelsCreateDialogController(Models) {
		
		var $ctrl = this;
		
		$ctrl.sendModel = sendModel;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource
		}
		
		function sendModel() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close);
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
