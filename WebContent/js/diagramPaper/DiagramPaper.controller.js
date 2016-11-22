define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = [
		'$scope', '$rootScope', '$timeout', '$uibModal', 'PreciseApi',
		'PreciseDiagramPaper', 'DiagramPaperToolset', 'MDLFiles', 'Pages', 'Tasks'
	];
	
	function DiagramController(
		$scope, $rootScope, $timeout, $uibModal, PreciseApi,
		PreciseDiagramPaper, DiagramPaperToolset, MDLFiles, Pages, Tasks
	) {
		var $ctrl = this;
		
		$ctrl.diagramToolset = DiagramPaperToolset;
		$ctrl.onPaperInit = onPaperInit;

		$ctrl.importDiagram = importDiagram;
		$ctrl.getFileURL = getFileURL;

		$ctrl.searchTextChanged = searchTextChanged;
		$ctrl.searchQueryChanged = searchQueryChanged;
		$ctrl.hideLocationsChanged = hideLocationsChanged;
		$ctrl.hideLabelsChanged = hideLabelsChanged;
		$ctrl.openLegend = openLegend;
		$ctrl.search = search;
		$ctrl.cancelSearch = cancelSearch;
		$ctrl.zoomChanged = zoomChanged;
		
		$ctrl.wrapInTimeout = wrapInTimeout;
		
		$ctrl.$onChanges = $onChanges;
		
		$ctrl.toolsets = ['File', 'Edit', 'View', 'Search'];
		$ctrl.toolset = 'Edit';
		
		$ctrl.showLocations = true;
		$ctrl.showLabels = true;
		
		var searchParamKeyMappings = {
			type: 'def'
		};
		
		function $onChanges(changes) {
			if (changes.model) {
				initSearch();
			}
			if (changes.currentWarning && $ctrl.diaPaper) {
				warningsChanged();
			}
		}
		
		function importDiagram() {
			$uibModal.open({
				component: 'preciseImportModel',
				resolve: {
					model: _.constant($ctrl.model),
					title: _.constant('Diagram'),
					subPath: _.constant(MDLFiles.DIAGRAM_PATH)
				}
			}).result
				.then(loadDiagram)
				.then($ctrl.onStructureChanged);
		}
		
		function getFileURL(model) {
			return MDLFiles.urlToModel(model, MDLFiles.DIAGRAM_PATH);
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
		
		function searchTextChanged() {
			search();
		}
		
		function searchQueryChanged() {
			$ctrl.searchQuery = _.chain($ctrl.searchParams)
				.keys()
				.filter(function (k) {
					return $ctrl.searchParams[k];
				})
				.map(function (k) {
					var val = $ctrl.searchParams[k];
					return (searchParamKeyMappings[k] || k)
						+ ':'
						+ (val.name || val)
				})
				.join(', ')
				.value();
			
			search();
		}
		
		function search() {
			if (!$ctrl.searchText && !$ctrl.searchQuery)
				cancelSearch();
			else {
				var req = $ctrl.advancedSearch
				? Tasks.searchAdvanced($ctrl.model.data, $ctrl.searchParams)
					: Tasks.searchSimple($ctrl.model.data, $ctrl.searchText);
				
				req.then(Pages.collectRemaining).then(function (tasks) {
					$ctrl.diaPaper.showSearchResults(tasks)
				});
			}
		}
		
		function cancelSearch() {
			$ctrl.searchParams = {};
			$ctrl.searchQuery = null;
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
			loadDiagram();
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
			
			$ctrl.paperPanZoom.setOnZoom(wrapInTimeout(onZoom));
			
			// init zoom
			onZoom($ctrl.paperPanZoom.getZoom());
		}
		
		function loadDiagram() {
			return PreciseDiagramPaper.toRawGraph(
				$ctrl.model,
				$ctrl.diaPaper.paper.getArea()
			).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			});
		}
		
		function wrapInTimeout(fn) {
			return _.partial($timeout, fn, 0, true);
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
			$ctrl.diaPaper.updateCell(data);
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
		
		function onZoom(scale) {
			$ctrl.zoomScale = Math.round(scale * 100);
		}
		
		function zoomChanged() {
			$ctrl.paperPanZoom.zoom($ctrl.zoomScale / 100);
		}
		
		function openLegend() {
			$uibModal.open({templateUrl: 'js/diagramPaper/diagramPaper.legend.html'});
		}
		
	}
	
	return DiagramController;

});