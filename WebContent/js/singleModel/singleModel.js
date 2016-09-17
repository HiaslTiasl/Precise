define([
	'lib/angular',
	'diagram/diagram',
	'api/api',
	'./SingleModel.service',
	'./SingleModel.controller',
	'./SingleModel-building.controller',
	'./SingleModel-phases.controller',
	'./SingleModel-taskTypes.controller',
	'./SingleModel-diagram.controller',
	'./taskProperties.component',
	'./dependencyProperties.component',
	'lib/ui-bootstrap',
	'lib/json-formatter'
], function (
	angular,
	preciseApi,
	preciseDiagram,
	SinlgeModelService,
	SingleModelController,
	SingleModelBuildingController,
	SingleModelPhasesController,
	SingleModelTaskTypesController,
	SingleModelDiagramController,
	taskPropertiesComponent,
	dependencyPropertiesComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', ['ui.bootstrap', 'jsonFormatter', preciseApi.name, preciseDiagram.name])
		.service('SingleModel', SinlgeModelService)
		.controller('SingleModelController', SingleModelController)
		.controller('SingleModelBuildingController', SingleModelBuildingController)
		.controller('SingleModelPhasesController', SingleModelPhasesController)
		.controller('SingleModelTaskTypesController', SingleModelTaskTypesController)
		.controller('SingleModelDiagramController', SingleModelDiagramController)
		.component('taskProperties', taskPropertiesComponent)
		.component('dependencyProperties', dependencyPropertiesComponent);
	
});