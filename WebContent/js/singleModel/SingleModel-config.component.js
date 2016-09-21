define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	return {
		templateUrl: 'js/singleModel/singleModel-config.html',
		controllerAs: '$ctrl',
		bindings: {
			model: '<'
		}
	}
	
});
