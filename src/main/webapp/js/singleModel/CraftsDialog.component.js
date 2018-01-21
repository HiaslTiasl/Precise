/**
 * Component for viewing and setting craft properties in a dialog.
 * @module "singleModel/singleModel-crafts.create-dialog.component
 */
define({
	templateUrl: 'js/singleModel/CraftsDialog.html',
	controller: 'DefaultResourceDialog',
	controllerAs: '$ctrl',
	bindings: {
		resolve: '<',
		modalInstance: '<'
	}
});
