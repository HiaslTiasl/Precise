define([
	'lib/angular',
	'./api.service'
], function (
	angular,
	ApiService
) {
	'use strict';
	
	return angular.module('precise.api', ['traverson'])
		.service('preciseApi', ApiService);
	
});