define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelController.$inject = ['$state'];
	
	function SingleModelController($state) {
		
		var $ctrl = this;
		
		$ctrl.$state = $state;
		
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
