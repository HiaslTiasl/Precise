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

		$ctrl.hideLocationsChanged = hideLocationsChanged;
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if ($ctrl.diaPaper) {
				if (changes.currentWarning)
					warningsChanged();
				if (changes.hideLocations)
					hideLocationsChanged();
			}
		}
		
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
			$scope.$on('render:done', onRenderDone);
			$scope.$on('properties:created', onPropertiesCreated);
			$scope.$on('properties:change', onPropertiesChange);
			$scope.$on('properties:cancel', onPropertiesCancel);
			$scope.$on('cell:deleted', onCellDeleted);
			
			// diagram -> remote
			$scope.$on('diagram:remove', onDiagramRemove);
		}
		
		function onRenderDone(event) {
			warningsChanged();
			hideLocationsChanged();
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
		
		function warningsChanged() {
			if ($ctrl.currentWarning)
				$ctrl.diaPaper.showWarningForTasks($ctrl.currentWarning.entities, true);
			else
				$ctrl.diaPaper.resetWarnings();
		}
		
		function hideLocationsChanged() {
			$ctrl.diaPaper.toggleHideLocations($ctrl.hideLocations);
		}
		
	}
	
	return DiagramController;

});