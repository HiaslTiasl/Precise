/**
 * Angular component for the diagram view of a single model.
 * @module "singleModel/SingleModel-diagram.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelDiagramController.$inject = ['$scope', '$uibModal', 'errorHandler', 'PreciseApi', 'TaskTypes', 'Tasks', 'Dependencies', 'Phases'];
	
	/**
	 * Controller constructor.
	 * @constructor
	 */
	function SingleModelDiagramController($scope, $uibModal, errorHandler, PreciseApi, TaskTypes, Tasks, Dependencies, Phases) {
		
		var $ctrl = this;
		
		$ctrl.showWarning = showWarning;

		$ctrl.done = done;
		$ctrl.cancelled = cancelled;
		$ctrl.taskDefinitionChanged = taskDefinitionChanged;
		$ctrl.loadWarnings = loadWarnings;
		
		$ctrl.$onChanges = $onChanges;
		
		var warningsLoaded = false;
		
		/** Specialized error handlers by operation and resource type. */
		var errorHandlers = {
			remove: {
				task: errorHandler.wrapIf(PreciseApi.isHttpConflict, {
					title: 'Cannot delete task',
					message: 'Remove all dependencies attached before deleting the task'
				}),
				dependency: errorHandler.wrapIf(PreciseApi.isHttpConflict, errorWithTitle('Cannot delete dependency')),
			},
			change: {
				task: errorHandler,
				taskDefinition: errorHandler,
				dependency: errorHandler
			},
			add: {
				task: errorHandler,
				dependency: errorHandler
			}
		};
		
		/** Resource wrappers by resource type. */
		var resourceWrappers = {
			task: Tasks.existingResource,
			dependency: Dependencies.existingResource
		};
		
		// Listen on DiagramPaper events
		$scope.$on('cell:delete', deleteCell);
		$scope.$on('task:new', newTaskHandler);
		$scope.$on('dependency:new', newDependencyHandler);
		$scope.$on('diagram:select', selectHandler);
		$scope.$on('task:change', diagramTaskChangeHandler);
		$scope.$on('dependency:change', diagramDependencyChangeHandler);
		
		function $onChanges(changes) {
			if (!warningsLoaded && changes.model) {
				// Initialize the list of warnings
				warningsLoaded = true;
				loadWarnings();
			}
		}
		
		/** Reset the current resource and resource type. */
		function resetResource() {
			$ctrl.resourceType = null;
			$ctrl.resource = null;
		}
		
		/**
		 * Creates a function that, given an error, returns a new error with
		 * the original message and the given title*/
		function errorWithTitle(title) {
			return function (error) {
				return {
					title: title,
					message: error.message
				};
			};
		}
		
		/** A task was changed from within the diagram, so update it on the server. */
		function diagramTaskChangeHandler(event, data) {
			Tasks
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('expandedTask');
				})
				.then(function (task) {
					// Notify diagram about change on the server
					$scope.$broadcast('properties:change', 'task', task);
				}, errorHandlers.change.task.handle);
		}
		
		/** A dependency was changed from within the diagram, so update it on the server. */
		function diagramDependencyChangeHandler(event, data) {
			Dependencies
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('dependencySummary');
				})
				.then(function (dependency) {
					// Notify diagram about change on the server
					$scope.$broadcast('properties:change', 'dependency', dependency);
				}, errorHandlers.change.dependency.handle);
		}
		
		/**
		 * A task definition was changed, so notify the diagram about change of all task
		 * boxes using that definition.
		 */
		function taskDefinitionChanged(data) {
			TaskTypes
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.getTasks({
						projection: Tasks.Resource.prototype.defaultProjection
					});
				})
				.then(function (tasks) {
					tasks.forEach(function (t) {
						$scope.$broadcast('properties:change', 'task', t);
					});
				}, errorHandlers.change.taskDefinition.handle);
		}
		
		/** Deletion of a cell on the server was requested, so do it and notify diagram. */
		function deleteCell(event, type, data) {
			PreciseApi.deleteResource(data)
				.then(function () {
					$scope.$broadcast('cell:deleted', type, data);
				}, errorHandlers.remove[type].handle);
		}
		
		/**
		 * Creation of a task on the server was requested, so open a modal dialog for
		 * entering (at least) mandatory fields, and create it when the dialog could
		 * be closed successfully.
		 */
		function newTaskHandler(event, data) {
			$uibModal.open({
				component: 'preciseCreateTask',
				resolve: {
					resource: _.constant(Tasks.newResource($ctrl.model, data)),
					phases: function () {
						return $ctrl.model.getPhases(Phases.Resource.prototype.defaultProjection)
					}
				}
			}).result.then(function (result) {
				$scope.$broadcast('properties:created', 'task', result);
			});
		}
		
		/**
		 * Creation of a dependency on the server was requested, so do it with
		 * default properties and notify diagram.
		 */
		function newDependencyHandler(event, data) {
			Dependencies
				.newResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('dependencySummary');
				})
				.then(function (dependency) {
					$scope.$broadcast('properties:created', 'dependency', dependency);
				}, errorHandlers.add.dependency.handle);
		}
		
		/** A cell was selected in the diagram, so open it in the properties view. */
		function selectHandler(event, resourceType, selectedView) {
			var data = selectedView && selectedView.model.get('data');
			showProperties(resourceType, data);
		}
		
		/** Show properties of the given resource type and data. */
		function showProperties(resourceType, data) {
			if (!data)
				resetResource();	// Close resource view if no data (e.g. a cell was unselected)
			else {
				// Wrap it in a resource to be shown
				resourceWrappers[resourceType]($ctrl.model, data).then(function (res) {
					$ctrl.resourceType = resourceType;
					$ctrl.resource = res;
				});
			}
		}
		
		/** Load the warnings of the model. */
		function loadWarnings() {
			$ctrl.model.getWarnings().then(function (warnings) {
				$ctrl.warnings = warnings;
			}, errorHandler.handle);
		}
		
		/** Highlight the given warning in the diagram. */
		function showWarning(w) {
			$ctrl.currentWarning = $ctrl.currentWarning !== w ? w : null;
		}
		
		/** 
		 * Editing of properties was confirmed, yielding the given result,
		 * so notify diagram and refresh the properties view.
		 */
		function done(result) {
			showProperties($ctrl.resourceType, result);
			$scope.$broadcast('properties:change', $ctrl.resourceType, result);
		}
		
		/** Editing of properties was cancelled, so notify diagram. */
		function cancelled() {
			$scope.$broadcast('properties:cancel', $ctrl.resourceType);
		}
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-diagram.html',
		controller: SingleModelDiagramController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			reload: '&'
		}
	};
});
