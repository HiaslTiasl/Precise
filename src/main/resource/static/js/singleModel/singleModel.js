/**
 * Angular module for the single model part.
 * @module "singleModule/singleModule"
 */
define([
	'lib/angular',
	'diagramPaper/diagramPaper',
	'api/api',
	'./SingleModel.component',
	'./Diagram.component',
	'./DiagramTaskDialog.component',
	'./Config.component',
	'./Building.component',
	'./Crafts.component',
	'./CraftsDialog.component',
	'./Activities.component',
	'./ActivitiesDialog.component',
	'./TaskProperties.component',
	'./DependencyProperties.component',
	'./ImportModelDialog.component',
	'lib/ui-bootstrap',
	'lib/json-formatter',
	'lib/angularjs-color-picker'
], function (
	angular,
	preciseApi,
	diagramPaper,
	SingleModelComponent,
	DiagramComponent,
	DiagramTaskDialogComponent,
	ConfigComponent,
	BuildingComponent,
	CraftsComponent,
	CraftsDialogComponent,
	ActivitiesComponent,
	ActivitiesDialogComponent,
	TaskPropertiesComponent,
	DependencyPropertiesComponent,
	ImportModelDialogComponent
) {
	'use strict';
	
	return angular.module('precise.singleModel', ['ui.bootstrap', 'jsonFormatter', 'color.picker', preciseApi.name, diagramPaper.name])
		.component('singleModel', SingleModelComponent)
		.component('diagram', DiagramComponent)
		.component('diagramTaskDialog', DiagramTaskDialogComponent)
		.component('config', ConfigComponent)
		.component('building', BuildingComponent)
		.component('crafts', CraftsComponent)
		.component('craftsDialog', CraftsDialogComponent)
		.component('activities', ActivitiesComponent)
		.component('activitiesDialog', ActivitiesDialogComponent)
		.component('taskProperties', TaskPropertiesComponent)
		.component('dependencyProperties', DependencyPropertiesComponent)
		.component('importModelDialog', ImportModelDialogComponent);
	
});