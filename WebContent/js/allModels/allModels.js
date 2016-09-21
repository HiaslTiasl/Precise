define([
	'lib/angular',
	'api/api',
	'singleModel/singleModel',
	'./AllModels.service',
	'./AllModels.component',
	'./allModels.create-dialog.component',
	'./allModels.run',
	'lib/smart-table',
	'lib/ng-file-upload',
	'lib/xeditable'
], function (
	angular,
	api,
	singleModel,
	AllModelsService,
	allModelsComponent,
	allModelsCreateDialogComponent,
	runner
) {
	'use strict';
	
	return angular.module('precise.allModels', ['smart-table', 'ngFileUpload', 'xeditable', api.name, singleModel.name])
		.service('AllModels', AllModelsService)
		.component('preciseAllModels', allModelsComponent)
		.component('preciseCreateModel', allModelsCreateDialogComponent)
		.run(runner);
	
});