define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelPhasesController.$inject = ['$scope', 'phases'];
	
	function SingleModelPhasesController($scope, phases) {
		
		var $ctrl = this;
		
		$ctrl.phases = phases;
		
	}
	
	return SingleModelPhasesController;
});
