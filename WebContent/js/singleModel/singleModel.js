define([
	'lib/angular',
	'diagram/diagram',
	'./singleModel.service',
	'./singleModel.controller',
	'./taskProperties.service',
	'./taskProperties.component',
	'./dependencyProperties.service',
	'./dependencyProperties.component'
], function (
	angular,
	preciseDiagram,
	SinlgeModelService,
	SingleModelController,
	TaskPropertiesService,
	taskPropertiesComponent,
	DependencyPropertiesService,
	dependencyPropertiesComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', [preciseDiagram.name])
		.service('singleModel', SinlgeModelService)
		.controller('SingleModelController', SingleModelController)
		.service('taskProperties', TaskPropertiesService)
		.component('taskProperties', taskPropertiesComponent)
		.service('dependencyProperties', DependencyPropertiesService)
		.component('dependencyProperties', dependencyPropertiesComponent);
	
});