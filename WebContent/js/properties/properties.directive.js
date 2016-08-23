define([
],function (
) {
	'use strict';
	
	taskPropertiesDirective.$inject = [];
	
	function taskPropertiesDirective() {
		return {
			templateUrl: 'js/properties/taskProperties.html',
			scope: {
				task: '=',
			}
		};
	}
	
	return taskPropertiesDirective;
	
});