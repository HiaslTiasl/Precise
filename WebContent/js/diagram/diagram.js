define([
	'lib/angular',
	'api/api',
	'./diagram.toolset',
	'./diagram.service',
	'./diagram.controller',
	'./diagram.directive',
	'lib/traverson-angular'
], function (
	angular,
	api,
	diagramToolset,
	DiagramService,
	DiagramController,
	diagramDirective
) {
	'use strict';
	
	return angular.module('precise.diagram', [api.name])
		.constant('diagramToolset', diagramToolset)
		.service('preciseDiagram', DiagramService)
		.controller('DiagramController', DiagramController)
		.directive('preciseDiagram', diagramDirective);
	
});