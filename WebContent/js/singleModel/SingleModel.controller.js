define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelController.$inject = ['$state', 'model'];
	
	function SingleModelController($state, model) {
		
		var $ctrl = this;
		
		$ctrl.$state = $state;
		$ctrl.model = model;
		
	}
	
	return SingleModelController;
});
