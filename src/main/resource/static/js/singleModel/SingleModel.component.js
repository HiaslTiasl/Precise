/**
 * Angular component for the single model part
 * @module "singleModel/SingleModel.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelController.$inject = ['$state', 'Models'];
	
	/**
	 * Controller constructor.
	 * @controller
	 */
	function SingleModelController($state, Models) {
		
		var $ctrl = this;
		
		$ctrl.$state = $state;	// Give the template access to ui-router state information
		$ctrl.reload = reload;

		/** Reload the model to update info on whether config and diagram are empty and editable. */
		function reload() {
			return $ctrl.model.reload('modelSummary')
				.then(Models.existingResource)
				.then(function (model) {
					return $ctrl.model = model;
				});
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel.html',
		controller: SingleModelController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<'
		}
	};
	
});
