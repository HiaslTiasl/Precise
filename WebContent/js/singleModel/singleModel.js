define([
	'lib/angular',
	'diagram/diagram',
	'./singleModel.service',
	'./singleModel.controller',
	'./taskProperties.component',
	'./dependencyProperties.component'
], function (
	angular,
	preciseDiagram,
	SinlgeModelService,
	SingleModelController,
	taskPropertiesComponent,
	dependencyPropertiesComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', [preciseDiagram.name])
		.service('singleModel', SinlgeModelService)
		.controller('SingleModelController', SingleModelController)
		.component('taskProperties', taskPropertiesComponent)
		.component('dependencyProperties', dependencyPropertiesComponent);
	
});