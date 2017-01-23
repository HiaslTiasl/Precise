/**
 * Angular module for the all-models part.
 * @module allModels/allModels
 */
define([
	'lib/angular',
	'api/api',
	'singleModel/singleModel',
	'./AllModels.service',
	'./AllModels.component',
	'./allModels.create-dialog.component',
	'lib/smart-table',
	'lib/ng-file-upload'
], function (
	angular,
	api,
	singleModel,
	AllModelsService,
	allModelsComponent,
	allModelsCreateDialogComponent
) {
	'use strict';
	
	return angular.module('precise.allModels', ['smart-table', 'ngFileUpload', api.name, singleModel.name])
		.service('AllModels', AllModelsService)
		.component('preciseAllModels', allModelsComponent)
		.component('preciseCreateModel', allModelsCreateDialogComponent);
	
});