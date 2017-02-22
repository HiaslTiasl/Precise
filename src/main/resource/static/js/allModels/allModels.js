/**
 * Angular module for the all-models part.
 * @module "allModels/allModels"
 */
define([
	'lib/angular',
	'api/api',
	'singleModel/singleModel',
	'./AllModels.service',
	'./AllModels.component',
	'./ModelsDialog.component',
	'lib/smart-table',
	'lib/ng-file-upload'
], function (
	angular,
	api,
	singleModel,
	AllModelsService,
	allModelsComponent,
	ModelsDialogComponent
) {
	'use strict';
	
	return angular.module('precise.allModels', ['smart-table', 'ngFileUpload', api.name, singleModel.name])
		.service('AllModels', AllModelsService)
		.component('allModels', allModelsComponent)
		.component('modelsDialog', ModelsDialogComponent);
	
});