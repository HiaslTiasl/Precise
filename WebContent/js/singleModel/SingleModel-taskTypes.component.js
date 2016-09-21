define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['Pages'];
	
	function SingleModelTaskTypesController(Pages) {
		
		var $ctrl = this;
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if ('model' in changes)
				loadTaskTypes();
		}
		
		function loadTaskTypes() {
			return $ctrl.model.getTaskTypes()
				.then(Pages.collectRemaining)
				.then(function (taskTypes) {
					$ctrl.taskTypes = taskTypes;
				});
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-taskTypes.html',
		controller: SingleModelTaskTypesController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<'
		}
	};
	
});
