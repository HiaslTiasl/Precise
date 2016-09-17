define([
	'lib/angular',
	'api/api',
	'singleModel/singleModel',
	'./AllModels.service',
	'./AllModels.controller',
	'./AllModelsCreateDialog.controller',
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
	AllModelsCreateDialogController,
	runner
) {
	'use strict';
	
	return angular.module('precise.allModels', ['smart-table', 'ngFileUpload', 'xeditable', api.name, singleModel.name])
		.service('AllModels', AllModelsService)
		.controller('AllModelsController', AllModelsController)
		.controller('AllModelsCreateDialogController', AllModelsCreateDialogController)
		.run(runner);
	
});