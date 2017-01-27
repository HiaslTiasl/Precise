/**
 * Angular module for working with a DiagramPaper.
 * @module "diagramPaper/diagramPaper"
 */
define([
	'lib/angular',
	'api/api',
	'./DiagramPaper.editToolset',
	'./DiagramPaper.service',
	'./DiagramPaper.controller',
	'./diagramPaper.directive',
	'lib/ui-bootstrap'
], function (
	angular,
	api,
	DiagramPaperEditToolset,
	DiagramPaperService,
	DiagramPaperController,
	diagramPaperDirective
) {
	'use strict';
	
	return angular.module('precise.diagram.paper', ['ui.bootstrap', api.name])
		.constant('DiagramPaperEditToolset', DiagramPaperEditToolset)
		.service('PreciseDiagramPaper', DiagramPaperService)
		.controller('DiagramPaperController', DiagramPaperController)
		.directive('preciseDiagramPaper', diagramPaperDirective);
	
});