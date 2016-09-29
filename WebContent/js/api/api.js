define([
	'lib/angular',
	'./PreciseApi.service',
	'./Files.service',
	'./MDLFiles.service',
	'./Pages.service',
	'./Resources.service',
	'./Models.service',
	'./Tasks.service',
	'./TaskTypes.service',
	'./Phases.service',
	'./Dependencies.service',
	'./withShortName.filter',
	'./join.filter'
], function (
	angular,
	ApiService,
	FilesService,
	MDLFilesService,
	PagesService,
	ResourcesService,
	ModelsService,
	TasksService,
	TaskTypesService,
	PhasesService,
	DependenciesService,
	withShortNameFilterFactory,
	joinFilterFactory
) {
	'use strict';
	
	return angular.module('precise.api', ['traverson'])
		.service('PreciseApi', ApiService)
		.service('Files', FilesService)
		.service('MDLFiles', MDLFilesService)
		.service('Pages', PagesService)
		.service('Resources', ResourcesService)
		.service('Models', ModelsService)
		.service('Tasks', TasksService)
		.service('TaskTypes', TaskTypesService)
		.service('Phases', PhasesService)
		.service('Dependencies', DependenciesService)
		.filter('withShortName', withShortNameFilterFactory)
		.filter('join', joinFilterFactory);
	
});