define([], function () {
	'use strict';
	
	SingleModelCraftCreateDialogController.$inject = ['Crafts'];
	
	function SingleModelCraftCreateDialogController(Crafts) {
		
		var $ctrl = this;
		
		$ctrl.sendCraft = sendCraft;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource
		}
		
		function sendCraft() {
			$ctrl.resource.send()
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
