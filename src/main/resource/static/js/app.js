define([
	'lib/angular',
	'api/api',
	'allModels/allModels',
	'singleModel/singleModel',
	'configStates',
	'configToastr',
	'angular-ui-router',
	'lib/angular-toastr'
], function (
	angular,
	api,
	allModels,
	singleModel,
	configStates,
	configToastr
) {
	'use strict';
	
	return angular.module('precise', ['ui.router', 'toastr', api.name, allModels.name, singleModel.name])
		.config(configStates)
		.config(configToastr);
	
});