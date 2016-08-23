define([
	'lib/angular',
	'api/api',
	'singleModel/singleModel',
	'./allModels.service',
	'./allModels.controller',
	'./allModels.run',
	'lib/smart-table',
	'lib/ng-file-upload',
	'lib/xeditable'
], function (
	angular,
	api,
	singleModel,
	AllModelsService,
	AllModelsController,
	runner
) {
	'use strict';
	
	return angular.module('precise.allModels', ['smart-table', 'ngFileUpload', 'xeditable', api.name, singleModel.name])
		.service('allModels', AllModelsService)
		.controller('AllModelsController', AllModelsController)
		.run(runner);
	
});