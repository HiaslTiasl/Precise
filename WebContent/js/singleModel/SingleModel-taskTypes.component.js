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
		$ctrl.deleteTaskType = deleteTaskType;
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model) {
				loadTaskTypes();
			}
		}
		
		function loadTaskTypes() {
			$ctrl.model.getTaskTypes({ projection: 'expandedTaskType' })
				.then(Pages.collectRemaining)
				.then(setTaskTypes, function (err) {
					console.log(err);
				});
		}
		
		function setTaskTypes(taskTypes) {
			$ctrl.taskTypes = taskTypes;
		}
		
		function createTaskType() {
			var modalInstance = $uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					model: _.constant($ctrl.model),
					phases: _.constant($ctrl.phases)
				}
			}).result.then(loadTaskTypes);
		}
		
		function deleteTaskType(taskType) {
			TaskTypes
				.existingResource($ctrl.model, taskType)
				.then(function (resource) {
					resource.delete();
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
