define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelController.$inject = ['$state', 'Models'];
	
	function SingleModelController($state, Models) {
		
		var $ctrl = this;
		
		$ctrl.$state = $state;
		
		$ctrl.reload = reload;
		
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
