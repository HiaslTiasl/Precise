define([], function () {
	'use strict';
	
	SingleModelCraftCreateDialogController.$inject = ['Crafts'];
	
	function SingleModelCraftCreateDialogController(Crafts) {
		
		var $ctrl = this;
		
		$ctrl.craft = {};
		
		$ctrl.createCraft = createCraft;
		$ctrl.cancel = cancel;
		
		function createCraft() {
			Crafts
				.newResource($ctrl.resolve.model, $ctrl.craft)
				.then(function (resource) {
					return resource.create();
				})
				.then($ctrl.modalInstance.close);
		}
		
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-crafts.create-dialog.html',
		controller: SingleModelCraftCreateDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
