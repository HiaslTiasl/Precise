define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = ['$scope', '$rootScope', '$timeout', 'PreciseApi', 'PreciseDiagramPaper', 'DiagramPaperToolset'];
	
	function DiagramController($scope, $rootScope, $timeout, PreciseApi, PreciseDiagraPaper, DiagramPaperToolset) {
		var $ctrl = this;
		
		$ctrl.diagramToolset = DiagramPaperToolset;
		$ctrl.onPaperInit = onPaperInit;
		
		function broadcast() {
			var args = arguments;
			$timeout(function () {
				$rootScope.$broadcast.apply($rootScope, args);
			});
		}
		
		function onPaperInit(paper) {
			$ctrl.diaPaper = new DiagramPaper(paper);
			attachListeners();
			PreciseDiagraPaper.toRawGraph($ctrl.model, paper.getArea()).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			});
		}
		
		function attachListeners() {
			
			// paper events -> scope events
			$ctrl.diaPaper.on('all', broadcast);
			
			// remote -> diagram
			$scope.$on('properties:created', onPropertiesCreated);
			$scope.$on('properties:change', onPropertiesChange);
			$scope.$on('properties:cancel', onPropertiesCancel);
			$scope.$on('cell:deleted', onCellDeleted);
			
			// diagram -> remote
			$scope.$on('diagram:remove', onDiagramRemove);
		}
		
		function onPropertiesCreated(event, type, data) {
			$ctrl.diaPaper.addCell(type, data);
			$ctrl.onStructureChanged();
		}
		
		function onPropertiesChange(event, type, data) {
			$ctrl.diaPaper.updateSelected(data);
		}
		
		function onPropertiesCancel(event, type) {
			$ctrl.diaPaper.unselect();
		}
		
		function onCellDeleted(event, type, data) {
			$ctrl.diaPaper.removeCell(type, data);
			$ctrl.onStructureChanged();
		}
		
		function onDiagramRemove(event, type, data) {
			PreciseApi.deleteResource(data)['catch'](function () {
				$ctrl.diaPaper.addCell(type, data);
			})
			.then($ctrl.onStructureChanged);
		}
		
	}
	
	return DiagramController;

});