define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelTaskTypesController.$inject = ['$uibModal'];
	
	function SingleModelTaskTypesController($uibModal) {
		var $ctrl = this;
		
		$ctrl.createTaskType = createTaskType;
		$ctrl.deleteTaskType = deleteTaskType;
		
		function createTaskType() {
			var modalInstance = $uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					phases: function () {
						return $ctrl.config.phases;
					}
				}
			});
			
			modalInstance.result.then(addTaskType);
		}
		
		function addTaskType(taskType) {
			$ctrl.config.taskTypes.push(taskType);
		}
		
		function deleteTaskType(index) {
			$ctrl.config.taskTypes.splice(index, 1);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-taskTypes.html',
		controller: SingleModelTaskTypesController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			config: '<'
		}
	};
	
});
