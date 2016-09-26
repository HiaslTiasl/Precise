define([
	'lib/angular',
	'diagramPaper/diagramPaper',
	'api/api',
	'./SingleModel.service',
	'./SingleModel.component',
	'./SingleModel-diagram.component',
	'./SingleModel-config.component',
	'./SingleModel-building.component',
	'./SingleModel-taskTypes.component',
	'./taskProperties.component',
	'./dependencyProperties.component',
	'lib/ui-bootstrap',
	'lib/json-formatter',
	'lib/angularjs-color-picker'
], function (
	angular,
	preciseApi,
	preciseDiagramPaper,
	SinlgeModelService,
	singleModelComponent,
	singleModelDiagramComponent,
	singleModelConfigComponent,
	singleModelBuildingComponent,
	singleModelTaskTypesComponent,
	taskPropertiesComponent,
	dependencyPropertiesComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', ['ui.bootstrap', 'jsonFormatter', 'color.picker', preciseApi.name, preciseDiagramPaper.name])
		.service('SingleModel', SinlgeModelService)
		.component('preciseSingleModel', singleModelComponent)
		.component('preciseDiagram', singleModelDiagramComponent)
		.component('preciseConfig', singleModelConfigComponent)
		.component('preciseBuilding', singleModelBuildingComponent)
		.component('preciseTaskTypes', singleModelTaskTypesComponent)
		.component('taskProperties', taskPropertiesComponent)
		.component('dependencyProperties', dependencyPropertiesComponent);
	
});