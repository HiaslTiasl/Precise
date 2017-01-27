/**
 * Angular component for the task definitions view of the configuration part.
 * @module "singleModel/SingleModel-taskTypes.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['$q', '$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'TaskTypes'];
	
	/**
	 * Controller constructor
	 * @constructor
	 */
	function SingleModelTaskTypesController($q, $uibModal, errorHandler, PreciseApi, Pages, TaskTypes) {
		var $ctrl = this;
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.editTaskType = editTaskType;
		$ctrl.deleteTaskType = deleteTaskType;
		
		$ctrl.$onChanges = $onChanges;
		
		/** Specialized error handler for conflicts on deletion. */
		var deleteErrorHandler = errorHandler.wrapIf(PreciseApi.isHttpConflict, {
			title: 'Cannot delete task definition',
			message: 'The tasks referencing this definition cannot be deleted automatically'
		});
		
		function $onChanges(changes) {
			if (changes.model) {
				loadTaskTypes();
			}
		}
		
		/** Loads the list of task definitions from the model. */
		function loadTaskTypes() {
			$ctrl.model.getTaskTypes({ projection: 'expandedTaskType' })
				.then(setTaskTypes, errorHandler.handle);
		}
		
		/** Sets the given list of task definitions. */
		function setTaskTypes(taskTypes) {
			$ctrl.taskTypes = taskTypes;
		}
		
		/** Returns a promise of the crafts of the model, and caches the result. */
		var getCrafts = _.once(function () {
			return $ctrl.model.getCrafts();
		});
		
		/** Opens the given task definition resource in a modal dialog. */
		function openModal(resource) {
			$uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					resource: resource,
					phases: _.constant($ctrl.phases),
					crafts: getCrafts
				}
			}).result.then(loadTaskTypes);
		}
		
		/** Opens a modal dialog for creating a task definition. */
		function createTaskType() {
			openModal(function () {
				return TaskTypes.newResource($ctrl.model);
			});
		}
		
		/** Opens a modal dialog for editing the given task definition. */
		function editTaskType(taskType) {
			openModal(function () {
				return TaskTypes.existingResource($ctrl.model, _.clone(taskType));
			});
		}
		
		/**
		 * Deletes the given task definition.
		 * If it has any task boxes, the user is prompted to confirm the deletion.
		 */
		function deleteTaskType(taskType) {
			var permission = !taskType.taskCount ? $q.when() :  PreciseApi.asyncConfirm([
				'There are tasks referencing this definition',
				'',
				'If you delete the definition, you will lose these tasks as well.',
				'Delete anyway?'
			].join('\n'));
			
			return permission.then(function () {
				return TaskTypes
					.existingResource($ctrl.model, taskType)
					.then(function (resource) {
						return resource.delete();
					})
					.then(loadTaskTypes, deleteErrorHandler.handle);
				// N.B. while it is possible to handle the success case outside in a chained
				// .then call, the same is not possible for handling the failure case, because
				// it would also be called if the user rejects the deletion.
			});
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-taskTypes.html',
		controller: SingleModelTaskTypesController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
