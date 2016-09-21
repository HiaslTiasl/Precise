define([], function () {
	'use strict';
	
	AllModelsCreateDialogController.$inject = ['Models'];
	
	function AllModelsCreateDialogController(Models) {
		
		var $ctrl = this;
		
		$ctrl.model = {};
		
		$ctrl.createModel = createModel;
		$ctrl.cancel = cancel;
		
		function createModel() {
			Models
				.newResource($ctrl.model)
				.create()
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
			modalInstance: '<'
		}
	}
	
});
