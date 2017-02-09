/**
 * Angular module for working with the backend server
 * @module "api/api"
 */
define([
	'lib/angular',
	'./PreciseApi.service',
	'./Files.service',
	'./RemoteFiles.service',
	'./MDLFiles.service',
	'./Scopes.service',
	'./OrderSpecifications.service',
	'./Pages.service',
	'./Resources.service',
	'./Models.service',
	'./Phases.service',
	'./Crafts.service',
	'./TaskTypes.service',
	'./Tasks.service',
	'./Dependencies.service',
	'./errorHandler.factory',
	'./DefaultResourceDialog.controller',
	'./errorDialog.component',
	'./withShortName.filter',
	'./join.filter',
	'lib/traverson-angular'
], function (
	angular,
	ApiService,
	FilesService,
	RemoteFilesService,
	MDLFilesService,
	ScopesService,
	OrderSpecificationsService,
	PagesService,
	ResourcesService,
	ModelsService,
	PhasesService,
	CraftsService,
	TaskTypesService,
	TasksService,
	DependenciesService,
	errorHandlerFactory,
	DefaultResourceDialogController,
	errorDialogComponent,
	withShortNameFilterFactory,
	joinFilterFactory
) {
	'use strict';
	
	return angular.module('precise.api', ['traverson'])
		.service('PreciseApi', ApiService)
		.service('Files', FilesService)
		.service('RemoteFiles', RemoteFilesService)
		.service('MDLFiles', MDLFilesService)
		.service('Scopes', ScopesService)
		.service('OrderSpecifications', OrderSpecificationsService)
		.service('Pages', PagesService)
		.service('Resources', ResourcesService)
		.service('Models', ModelsService)
		.service('Phases', PhasesService)
		.service('Crafts', CraftsService)
		.service('TaskTypes', TaskTypesService)
		.service('Tasks', TasksService)
		.service('Dependencies', DependenciesService)
		.factory('errorHandler', errorHandlerFactory)
		.controller('DefaultResourceDialog', DefaultResourceDialogController)
		.component('preciseErrorDialog', errorDialogComponent)
		.filter('withShortName', withShortNameFilterFactory)
		.filter('join', joinFilterFactory);
	
});