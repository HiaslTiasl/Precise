define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = ['$scope', '$rootScope', '$timeout', '$uibModal', 'PreciseApi', 'PreciseDiagramPaper', 'DiagramPaperToolset', 'Pages', 'Tasks'];
	
	function DiagramController($scope, $rootScope, $timeout, $uibModal, PreciseApi, PreciseDiagraPaper, DiagramPaperToolset, Pages, Tasks) {
		var $ctrl = this;
		
		$ctrl.diagramToolset = DiagramPaperToolset;
		$ctrl.onPaperInit = onPaperInit;

		$ctrl.hideLocationsChanged = hideLocationsChanged;
		$ctrl.hideLabelsChanged = hideLabelsChanged;
		$ctrl.openLegend = openLegend;
		$ctrl.search = search;
		$ctrl.cancelSearch = cancelSearch;
		
		$ctrl.$onChanges = $onChanges;
		
		$ctrl.toolsets = ['Edit', 'View', 'Search'];
		$ctrl.toolset = 'Edit';
		
		$ctrl.showLocations = true;
		$ctrl.showLabels = true;
		
		$ctrl.advancedSearchMode = false;
		
		function $onChanges(changes) {
			if (changes.model) {
				initSearch();
			}
			if (changes.currentWarning && $ctrl.diaPaper) {
				warningsChanged();
			}
		}
		
		function initSearch() {
			$ctrl.searchParams = {};
			$ctrl.model.getPhases().then(function (phases) {
				$ctrl.phases = phases;
			});
			$ctrl.model.getTaskTypes().then(function (taskTypes) {
				$ctrl.taskTypes = taskTypes;
			});
			$ctrl.model.getCrafts().then(function (crafts) {
				$ctrl.crafts = crafts;
			});
		}
		
		function search() {
			var req = $ctrl.advancedSearchMode
				? Tasks.searchAdvanced($ctrl.model.data, $ctrl.searchParams)
				: Tasks.searchSimple($ctrl.model.data, $ctrl.searchText);
				
			req.then(Pages.collectRemaining).then(function (tasks) {
				$ctrl.diaPaper.showSearchResults(tasks)
			});
		}
		
		function cancelSearch() {
			$ctrl.searchParams = {};
			$ctrl.searchText = null;
			$ctrl.diaPaper.resetSearchResults();
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
				$ctrl.diaPaper.showWarning($ctrl.currentWarning);
			else
				$ctrl.diaPaper.resetWarnings();
		}
		
		function hideLocationsChanged() {
			$ctrl.diaPaper.toggleHideLocations(!$ctrl.showLocations);
		}
		
		function hideLabelsChanged() {
			$ctrl.diaPaper.toggleHideLabels(!$ctrl.showLabels);
		}
		
		function openLegend() {
			$uibModal.open({templateUrl: 'js/diagramPaper/diagramPaper.legend.html'});
		}
		
	}
	
	return DiagramController;

});