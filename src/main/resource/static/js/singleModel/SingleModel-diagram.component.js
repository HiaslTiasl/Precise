define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelDiagramController.$inject = ['$scope', '$uibModal', 'errorHandler', 'PreciseApi', 'TaskTypes', 'Tasks', 'Dependencies', 'Phases'];
	
	function SingleModelDiagramController($scope, $uibModal, errorHandler, PreciseApi, TaskTypes, Tasks, Dependencies, Phases) {
		
		var $ctrl = this;
		
		$ctrl.showWarning = showWarning;

		$ctrl.done = done;
		$ctrl.cancelled = cancelled;
		$ctrl.taskDefinitionChanged = taskDefinitionChanged;
		$ctrl.loadWarnings = loadWarnings;
		
		$ctrl.$onChanges = $onChanges;
		
		var warningsLoaded = false;
		
		var errorHandlers = {
			remove: {
				task: errorHandler.wrapIf(PreciseApi.isHttpConflict, {
					title: 'Cannot delete task',
					message: 'Remove all dependencies attached before deleting the task'
				}),
				dependency: errorHandler.wrapIf(PreciseApi.isHttpConflict, titleErrorWrapper('Cannot delete dependency')),
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
		
		var resourceWrappers = {
			task: Tasks.existingResource,
			dependency: Dependencies.existingResource
		};
		
		$scope.$on('cell:delete', deleteCell);
		$scope.$on('task:new', newTaskHandler);
		$scope.$on('dependency:new', newDependencyHandler);
		$scope.$on('diagram:select', selectHandler);
		$scope.$on('task:change', diagramTaskChangeHandler);
		$scope.$on('dependency:change', diagramDependencyChangeHandler);
		
		function $onChanges(changes) {
			if (!warningsLoaded && changes.model) {
				warningsLoaded = true;
				loadWarnings();
			}
				
		}
		
		function resetResource() {
			$ctrl.resourceType = null;
			$ctrl.resource = null;
		}
		
		function titleErrorWrapper(title) {
			return function (error) {
				return {
					title: title,
					message: error.message
				};
			};
		}
		
		function diagramTaskChangeHandler(event, data) {
			Tasks
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('expandedTask');
				})
				.then(function (task) {
					$scope.$broadcast('properties:change', 'task', task);
				}, errorHandlers.change.task.handle);
		}
		
		function diagramDependencyChangeHandler(event, data) {
			Dependencies
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('dependencySummary');
				})
				.then(function (dependency) {
					$scope.$broadcast('properties:change', 'dependency', dependency);
				}, errorHandlers.change.dependency.handle);
		}
		
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
		
		function deleteCell(event, type, data) {
			PreciseApi.deleteResource(data)
				.then(function () {
					$scope.$broadcast('cell:deleted', type, data);
				}, errorHandlers.remove[type].handle);
		}
		
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
		
		function selectHandler(event, resourceType, selectedView) {
			var data = selectedView && selectedView.model.get('data');
			showProperties(resourceType, data);
		}
		
		function showProperties(resourceType, data) {
			if (!data)
				resetResource();
			else {
				resourceWrappers[resourceType]($ctrl.model, data).then(function (res) {
					$ctrl.resourceType = resourceType;
					$ctrl.resource = res;
				});
			}
		}
		
		function loadWarnings() {
			$ctrl.model.getWarnings().then(function (warnings) {
				$ctrl.warnings = warnings;
			}, errorHandler.handle);
		}
		
		function showWarning(w) {
			$ctrl.currentWarning = $ctrl.currentWarning !== w ? w : null;
		}
		
		function done(result) {
			showProperties($ctrl.resourceType, result);
			$scope.$broadcast('properties:change', $ctrl.resourceType, result);
		}
		
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
