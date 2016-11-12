define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['$uibModal', 'Pages', 'TaskTypes'];
	
	function SingleModelTaskTypesController($uibModal, Pages, TaskTypes) {
		var $ctrl = this;
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.editTaskType = editTaskType;
		$ctrl.deleteTaskType = deleteTaskType;
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model) {
				loadTaskTypes();
			}
		}
		
		function loadTaskTypes() {
			$ctrl.model.getTaskTypes({ projection: 'expandedTaskType' })
				.then(setTaskTypes, function (err) {
					console.log(err);
				});
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
			TaskTypes
				.existingResource($ctrl.model, taskType)
				.then(function (resource) {
					return resource.delete();
				})
				.then(loadTaskTypes);
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
