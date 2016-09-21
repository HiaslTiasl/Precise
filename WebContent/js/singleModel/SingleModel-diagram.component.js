define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelDiagramController.$inject = ['$scope', 'PreciseApi', 'Tasks', 'Dependencies'];
	
	function SingleModelDiagramController($scope, PreciseApi, Tasks, Dependencies) {
		
		var $ctrl = this;
		
		$ctrl.done = done;
		$ctrl.cancelled = cancelled;
		
		$ctrl.$onChanges = $onChanges;
		
		$scope.$on('cell:delete', deleteCell);
		$scope.$on('task:new', newTaskHandler);
		$scope.$on('dependency:new', newDependencyHandler);
		$scope.$on('task:select', selectTaskHandler);
		$scope.$on('dependency:select', selectDependencyHandler);
		$scope.$on('task:change', diagramTaskChangeHandler);
		$scope.$on('dependency:change', diagramDependencyChangeHandler);
		
		function $onChanges(changes) {
			if (changes.model)
				resetResources();
		}
		
		function resetResources() {
			$ctrl.resourceType = null;
			$ctrl.activeResource = $ctrl.dependencyResource = $ctrl.taskResource = null;
		}
		
		function diagramTaskChangeHandler(event, data) {
			Tasks
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('expandedTask');
				})
				.then(function (task) {
					$scope.$broadcast('properties:change', 'task', task);
				});
		}
		
		function diagramDependencyChangeHandler(event, data) {
			Dependencies
				.existingResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('dependencySummary');
				})
				.then(function (dependency) {
					$scope.$broadcast('properties:change', 'dependency', dependency);
				});
		}
		
		function deleteCell(event, type, data) {
			PreciseApi.deleteResource(data)
				.then(function () {
					$scope.$broadcast('cell:deleted', type, data);
				});
		}
		
		function newTaskHandler(event, data) {
			Tasks
				.newResource($ctrl.model, data)
				.then(function (resource) {
					$ctrl.resourceType = 'task';
					$ctrl.activeResource = $ctrl.taskResource = resource;
					$ctrl.dependencyResource = null;
				})
		}
		
		function newDependencyHandler(event, data) {
			Dependencies
				.newResource($ctrl.model, data)
				.then(function (resource) {
					return resource.send('dependencySummary');
				})
				.then(function (dependency) {
					$scope.$broadcast('properties:created', 'dependency', dependency);
				});
		}
		
		function selectTaskHandler(event, selectedView) {
			var data = selectedView && selectedView.model.get('data');
			$ctrl.dependencyResource = null;
			if (!data)
				resetResources();
			else {
				Tasks.existingResource($ctrl.model, data).then(function (res) {
					$ctrl.resourceType = 'task';
					$ctrl.activeResource = $ctrl.taskResource = res;
					$ctrl.dependencyResource = null;
				});
			}
		}
		
		function selectDependencyHandler(event, selectedView) {
			var data = selectedView && selectedView.model.get('data');
			if (!data)
				resetResources();
			else {
				Dependencies.existingResource($ctrl.model, data).then(function (res) {
					$ctrl.resourceType = 'dependency';
					$ctrl.activeResource = $ctrl.dependencyResource = res;
					$ctrl.taskResource = null;
				});
			}
		}
		
		function done(result) {
			if ($ctrl.activeResource.exists)
				$scope.$broadcast('properties:change', $ctrl.resourceType, result);
			else
				$scope.$broadcast('properties:created', $ctrl.resourceType, result);
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
			model: '<'
		}
	};
});
