/**
 * Component for viewing and setting model properties in a dialog.
 * @module "allModels/allModels.create-dialog.component"
 */
define({
	templateUrl: 'js/allModels/ModelsDialog.html',
	controller: 'DefaultResourceDialog',
	controllerAs: '$ctrl',
	bindings: {
		resolve: '<',
		modalInstance: '<'
	}
});
