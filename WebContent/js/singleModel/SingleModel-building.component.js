define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	return {
		templateUrl: 'js/singleModel/singleModel-building.html',
		controllerAs: '$ctrl',
		bindings: {
			config: '<'
		}
	};
	
});
