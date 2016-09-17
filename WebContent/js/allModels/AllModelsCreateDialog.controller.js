define([], function () {
	'use strict';
	
	AllModelsCreateDialogController.$inject = ['$uibModalInstance', 'Models'];
	
	function AllModelsCreateDialogController($uibModalInstance, Models) {
		
		var $ctrl = this;
		
		$ctrl.model = {};
		$ctrl.createModel = createModel;
		$ctrl.cancel = cancel;
		
		function createModel() {
			Models
				.newResource($ctrl.model)
				.create()
				.then($uibModalInstance.close);
		}
		
		function cancel() {
			$uibModalInstance.dismiss('cancel');
		}
		
	}
	
	return AllModelsCreateDialogController;
	
});
