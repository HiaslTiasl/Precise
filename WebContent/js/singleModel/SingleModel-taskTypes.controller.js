define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['$scope', 'taskTypes'];
	
	function SingleModelTaskTypesController($scope, taskTypes) {
		
		var $ctrl = this;
		
		$ctrl.taskTypes = taskTypes;
		
	}
	
	return SingleModelTaskTypesController;
});
