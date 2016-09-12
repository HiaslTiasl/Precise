define([
	'lib/angular',
	'./api.service',
	'./TaskResource.factory',
	'./DependencyResource.factory'
], function (
	angular,
	ApiService,
	taskResourceFactory,
	dependencyResourceFactory
) {
	'use strict';
	
	return angular.module('precise.api', ['traverson'])
		.service('preciseApi', ApiService)
		.factory('TaskResource', taskResourceFactory)
		.factory('DependencyResource', dependencyResourceFactory);
	
});