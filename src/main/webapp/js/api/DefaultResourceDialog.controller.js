/**
 * Default controller implementation for dialogs showing properties of resources
 * that can be used to edit or create resources.
 * @module "allModels/allModels.create-dialog.component"
 */
define([], function () {
	'use strict';
	
	DefaultResourceDialogController.$inject = ['errorHandler'];
	
	/**
	 * Controller for a modal dialog showing a resource.
	 * @constructor
	 */
	function DefaultResourceDialogController(errorHandler) {
		
		var $ctrl = this;
		
		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource
		}
		
		/** Sends the resource to the server to apply the changes and closes the dialog. */
		function send() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close, errorHandler.handle);
		}
		
		/** Cancels the changes and closes the dialog. */
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return DefaultResourceDialogController;
	
});
