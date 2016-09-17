define([
	'lib/angular',
	'api/api',
	'./Diagram.toolset',
	'./Diagram.service',
	'./Diagram.controller',
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
		.constant('DiagramToolset', diagramToolset)
		.service('PreciseDiagram', DiagramService)
		.controller('DiagramController', DiagramController)
		.directive('preciseDiagram', diagramDirective);
	
});