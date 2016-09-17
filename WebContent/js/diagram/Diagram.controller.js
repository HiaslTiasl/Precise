define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = ['$scope', '$rootScope', '$timeout', 'PreciseApi', 'PreciseDiagram', 'DiagramToolset'];
	
	function DiagramController($scope, $rootScope, $timeout, PreciseApi, PreciseDiagram, DiagramToolset) {
		var $ctrl = this;
		
		$ctrl.diagramToolset = DiagramToolset;
		$ctrl.onPaperInit = onPaperInit;
		
		// remote -> diagram
		$scope.$on('properties:created', onPropertiesCreated);
		$scope.$on('properties:change', onPropertiesChange);
		$scope.$on('properties:cancel', onPropertiesCancel);
		$scope.$on('cell:deleted', onCellDeleted);
		
		// diagram -> remote
		$scope.$on('diagram:remove', onDiagramRemove);
		
		function broadcast() {
			var args = arguments;
			$timeout(function () {
				$rootScope.$broadcast.apply($rootScope, args);
			});
		}
		
		function onPaperInit(paper) {
			$ctrl.diaPaper = new DiagramPaper(paper);
			$ctrl.diaPaper.on('all', broadcast);
			PreciseDiagram.toRawGraph($ctrl.model, paper.getArea()).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			});
		}
		
		function onPropertiesCreated(event, type, data) {
			$ctrl.diaPaper.addCell(type, data);
		}
		
		function onPropertiesChange(event, type, data) {
			$ctrl.diaPaper.updateSelected(data);
		}
		
		function onPropertiesCancel(event, type) {
			$ctrl.diaPaper.unselect();
		}
		
		function onCellDeleted(event, type, data) {
			$ctrl.diaPaper.removeCell(type, data);
		}
		
		function onDiagramRemove(event, type, data) {
			PreciseApi.deleteResource(data)['catch'](function () {
				$ctrl.diaPaper.addCell(type, data);
			});
		}
		
	}
	
	return DiagramController;

});