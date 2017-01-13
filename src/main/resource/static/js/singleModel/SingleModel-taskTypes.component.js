define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['$q', '$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'TaskTypes'];
	
	function SingleModelTaskTypesController($q, $uibModal, errorHandler, PreciseApi, Pages, TaskTypes) {
		var $ctrl = this;
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.editTaskType = editTaskType;
		$ctrl.deleteTaskType = deleteTaskType;
		
		$ctrl.$onChanges = $onChanges;
		
		var MSG_DEF_HAS_TASKS = 'There are tasks referencing this definition';
		
		var deleteErrorHandler = errorHandler.wrapIf(PreciseApi.isHttpConflict, {
			title: 'Cannot delete task definition',
			message: 'The tasks referencing this definition cannot be deleted automatically'
		});
		
		function $onChanges(changes) {
			if (changes.model) {
				loadTaskTypes();
			}
		}
		
		function loadTaskTypes() {
			$ctrl.model.getTaskTypes({ projection: 'expandedTaskType' })
				.then(setTaskTypes, errorHandler.handle);
		}
		
		function setTaskTypes(taskTypes) {
			$ctrl.taskTypes = taskTypes;
		}
		
		var getCrafts = _.once(function () {
			return $ctrl.model.getCrafts();
		});
		
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
		
		function editTaskType(taskType) {
			openModal(function () {
				return TaskTypes.existingResource($ctrl.model, _.clone(taskType));
			});
		}
		
		function createTaskType() {
			openModal(function () {
				return TaskTypes.newResource($ctrl.model);
			});
		}
		
		function deleteTaskType(taskType) {
			var permission = !taskType.taskCount ? $q.when() :  PreciseApi.asyncConfirm([
				MSG_DEF_HAS_TASKS,
				'',
				'If you delete the definition, you will lose these tasks as well.',
				'Delete anyway?'
			].join('\n'));
			
			permission.then(function () {
				TaskTypes
					.existingResource($ctrl.model, taskType)
					.then(function (resource) {
						return resource.delete();
					})
					.then(loadTaskTypes, deleteErrorHandler.handle);
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
