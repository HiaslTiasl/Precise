/**
 * Angular controller for the DiagramPaper.
 * @module "diagramPaper/DiagramPaper.controller"
 */
define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = [
		'$scope', '$rootScope', '$q', '$timeout', '$uibModal', 'errorHandler', 'PreciseApi',
		'PreciseDiagramPaper', 'DiagramPaperEditToolset', 'MDLFiles', 'Pages', 'Tasks'
	];
	
	/**
	 * Controller constructor.
	 * @constructor
	 */
	function DiagramController(
		$scope, $rootScope, $q, $timeout, $uibModal, errorHandler, PreciseApi,
		PreciseDiagramPaper, DiagramPaperEditToolset, MDLFiles, Pages, Tasks
	) {
		var $ctrl = this;
		
		$ctrl.diagramEditToolset = DiagramPaperEditToolset;
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
		
		$ctrl.toolsets = ['File', 'Edit', 'View', 'Search'];	// Toolset titles in display order
		$ctrl.toolset = 'Edit';
		
		$ctrl.showLocations = true;
		$ctrl.showLabels = true;
		
		// Map query keys as expected by backend to keys to be displayed
		// in the query preview
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
		
		/** Opens a dialog for importing the diagram part of a model. */
		function importDiagram() {
			$uibModal.open({
				component: 'preciseImportModel',
				resolve: {
					model: _.constant($ctrl.model),
					title: _.constant('Diagram'),
					mdlContext: _.constant(MDLFiles.diagram)
				}
			}).result
				.then(loadDiagram)
				.then(structuralDiagramChange);
		}
		
		/** Returns the URI of the MDL file of diagram part of the given model. */
		function getFileURL(model) {
			return MDLFiles.diagram.getModelUrl(model);
		}
		
		/** Fetches additional data required for the search functionality. */
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
		
		/** The search query text was changed, so update the search result. */
		function searchTextChanged() {
			search();
		}
		
		/** A search parameter was changed, so update the preview text and the search result. */
		function searchQueryChanged() {
			// Update the preview text of the form key:value(, key:value)*
			$ctrl.searchQuery = _.chain($ctrl.searchParams)
				.keys()
				.filter(function (k) {
					return $ctrl.searchParams[k];	// Only show truthy values
				})
				.map(function (k) {
					var val = $ctrl.searchParams[k];
					return (searchParamKeyMappings[k] || k)
						+ ':'
						+ (val.name || val)
				})
				.join(', ')
				.value();
			// Update the search result
			search();
		}
		
		/** Updates the search result. */
		function search() {
			// The query is empty, so unhighlight the previous search result
			if (!$ctrl.searchText && !$ctrl.searchQuery)
				cancelSearch();
			else {
				// Perform a new search, either simple or advanced
				var req = $ctrl.advancedSearch
					? Tasks.searchAdvanced($ctrl.model.data, $ctrl.searchParams)
					: Tasks.searchSimple($ctrl.model.data, $ctrl.searchText);
				// Highlight the returned results
				req.then(Pages.collectRemaining).then(function (tasks) {
					$ctrl.diaPaper.showSearchResults(tasks)
				}, errorHandler.handle);
			}
		}
		
		/** Resets the search results, i.e. unhighlights the currently highlighted tasks. */
		function cancelSearch() {
			$ctrl.searchParams = {};
			$ctrl.searchQuery = null;
			$ctrl.searchText = null;
			$ctrl.diaPaper.resetSearchResults();
		}
		
		/** Helper method for broadcasting an angular event from the $rootScope using the given arguments. */
		function broadcast() {
			var args = arguments;
			// Wrap in a $timeout to avoid depending on whether an angular $digest clycle is already in progress.
			$timeout(function () {
				$rootScope.$broadcast.apply($rootScope, args);
			});
		}
		
		/** A JointJS paper was initialized. */
		function onPaperInit(paper) {
			// Wrap it in a DiagramPaper
			$ctrl.diaPaper = new DiagramPaper(paper);
			attachListeners();
			loadDiagram();
		}
		
		/** Attaches listeners to the DiagramPaper, to the $scope, and to the paperPanZoom instance. */
		function attachListeners() {
			// Re-trigger all BackboneJS events from the paper as AngularJS events,
			// broadcasted from the $rootScope, so the rest of the application
			// does not have to know that we are using BackboneJS and JointJS at all.
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
		
		/** Loads the diagram data from the server and displays it in the DiagramPaper. */
		function loadDiagram() {
			return PreciseDiagramPaper.toRawGraph(
				$ctrl.model,
				$ctrl.diaPaper.paper.getArea()
			).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			}, errorHandler.handle);
		}
		
		/** Helper function for wrapping the given function in a call to $timeout. */
		function wrapInTimeout(fn) {
			return _.partial($timeout, fn, 0, true);
		}
		
		/**
		 * The initial data from the server was processed and rendered in the paper,
		 * so update the highlighted and hidden shapes.
		 */
		function onRenderDone(event) {
			warningsChanged();
			hideLocationsChanged();
			hideLabelsChanged();
		}
		
		/** An entity of the given type and data was created from outside, so add it to the diagram. */
		function onPropertiesCreated(event, type, data) {
			$ctrl.diaPaper.addCell(type, data);
			// This is a structural change, because the set of cells changed
			structuralDiagramChange();
		}
		
		/** An entity of the given type and data was updated from outside, so update it in the diagram. */
		function onPropertiesChange(event, type, data) {
			$ctrl.diaPaper.updateCell(data);
			// This is a non-structural change, because the set of cells did not change, only properties
			nonStructuralDiagramChange();
		}
		
		/** Editing of an entity of the given type was cancelled, so unselected the corresponding cell view. */
		function onPropertiesCancel(event, type) {
			$ctrl.diaPaper.unselect();
		}
		
		/** An entity of the given type and data was deleted from outside, so remove it from the diagram. */
		function onCellDeleted(event, type, data) {
			$ctrl.diaPaper.removeCell(type, data);
			// This is a structural change, because the set of cells changed
			structuralDiagramChange();
		}
		
		/** An entity of the given type and data was deleted from within the diagram, so delete it on the server. */
		function onDiagramRemove(event, type, data) {
			PreciseApi.deleteResource(data)['catch'](function () {
				$ctrl.diaPaper.addCell(type, data);
			})
			.then(structuralDiagramChange, errorHandler.handle);	// This is a structural change, because the set of cells changed
		}
		
		/** Updates the currently displayed warning. */
		function warningsChanged() {
			if ($ctrl.currentWarning)
				$ctrl.diaPaper.showWarning($ctrl.currentWarning);
			else
				$ctrl.diaPaper.resetWarnings();
		}
		
		/** Toggles visibility of the locations according to the current user input. */
		function hideLocationsChanged() {
			$ctrl.diaPaper.toggleHideLocations(!$ctrl.showLocations);
		}
		
		/** Toggles visibility of the dependency labels according to the current user input. */
		function hideLabelsChanged() {
			$ctrl.diaPaper.toggleHideLabels(!$ctrl.showLabels);
		}
		
		/** The paper was zoomed, so update the zoom scale. */
		function onZoom(scale) {
			$ctrl.zoomScale = Math.round(scale * 100);	// show scale as percentage
		}
		
		/** The zoom scale was changed by the user, so zoom the paper accordingly. */
		function zoomChanged() {
			$ctrl.paperPanZoom.zoom($ctrl.zoomScale / 100);
		}
		
		/** Open a legend of the diagram. */
		function openLegend() {
			$uibModal.open({templateUrl: 'js/diagramPaper/diagramPaper.legend.html'});
		}
		
		/** There was a structural change in the diagram, so call the corresponding functions. */
		function structuralDiagramChange() {
			$ctrl.onStructureChanged();
			$ctrl.onDiagramChanged();
		}
		
		/** There was a non-structural change in the diagram, so call the corresponding function. */
		function nonStructuralDiagramChange() {
			$ctrl.onDiagramChanged();
		}
		
	}
	
	return DiagramController;

});