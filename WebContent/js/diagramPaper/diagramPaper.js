define([
	'lib/angular',
	'api/api',
	'./DiagramPaper.toolset',
	'./DiagramPaper.service',
	'./DiagramPaper.controller',
	'./diagramPaper.directive',
	'lib/traverson-angular'
], function (
	angular,
	api,
	diagramPaperToolset,
	DiagramPaperService,
	DiagramPaperController,
	diagramPaperDirective
) {
	'use strict';
	
	return angular.module('precise.diagram.paper', [api.name])
		.constant('DiagramPaperToolset', diagramPaperToolset)
		.service('PreciseDiagramPaper', DiagramPaperService)
		.controller('DiagramPaperController', DiagramPaperController)
		.directive('preciseDiagramPaper', diagramPaperDirective);
	
});