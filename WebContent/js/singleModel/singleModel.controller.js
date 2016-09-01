define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelController.$inject = ['$scope', 'model'];
	
	function SingleModelController($scope, model) {
		
		var $ctrl = this;
		
		$ctrl.model = model;
		
		$ctrl.done = done;
		$ctrl.cancelled = cancelled;
		
		$scope.$on('task:select', createSelectionHandler('task'));
		$scope.$on('dependency:select', createSelectionHandler('dependency'));
		
		function createSelectionHandler(type) {
			return function (event, selectedView) {
				var resource = selectedView && selectedView.model.get('data');
				$ctrl.viewType = resource && type;
				$ctrl.resource = resource;
			};
		}
		
		function done(result) {
			$scope.$broadcast('properties:change', $ctrl.viewType, result);
		}
		
		function cancelled() {
			$scope.$broadcast('properties:cancel', $ctrl.viewType);
		}
	}
	
	return SingleModelController;
});
