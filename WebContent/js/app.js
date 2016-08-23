define([
	'lib/angular',
	'api/api',
	'allModels/allModels',
	'singleModel/singleModel',
	'configStates',
	'lib/angular-ui-router',
], function (
	angular,
	api,
	allModels,
	singleModel,
	configStates
) {
	'use strict';
	
	return angular.module('precise', ['ui.router', api.name, allModels.name, singleModel.name])
		.config(configStates);
	
});