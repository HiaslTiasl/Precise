define([
	'lib/angular',
	'diagramPaper/diagramPaper',
	'api/api',
	'./SingleModel.service',
	'./SingleModel.component',
	'./SingleModel-diagram.component',
	'./singleModel-diagram.create-task-dialog.component',
	'./SingleModel-config.component',
	'./singleModel-config.import-dialog.component',
	'./SingleModel-building.component',
	'./SingleModel-taskTypes.component',
	'./singleModel-taskTypes.create-dialog.component',
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
	singleModelDiagramCreateTaskDialogComponent,
	singleModelConfigComponent,
	singleModelConfigImportDialogComponent,
	singleModelBuildingComponent,
	singleModelTaskTypesComponent,
	singleModelTaskTypesCreateDialogComponent,
	taskPropertiesComponent,
	dependencyPropertiesComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', ['ui.bootstrap', 'jsonFormatter', 'color.picker', preciseApi.name, preciseDiagramPaper.name])
		.service('SingleModel', SinlgeModelService)
		.component('preciseSingleModel', singleModelComponent)
		.component('preciseDiagram', singleModelDiagramComponent)
		.component('preciseCreateTask', singleModelDiagramCreateTaskDialogComponent)
		.component('preciseConfig', singleModelConfigComponent)
		.component('preciseImportConfig', singleModelConfigImportDialogComponent)
		.component('preciseBuilding', singleModelBuildingComponent)
		.component('preciseTaskTypes', singleModelTaskTypesComponent)
		.component('preciseCreateTaskType', singleModelTaskTypesCreateDialogComponent)
		.component('taskProperties', taskPropertiesComponent)
		.component('dependencyProperties', dependencyPropertiesComponent);
	
});