define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelCraftsController.$inject = ['$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'Crafts'];
	
	function SingleModelCraftsController($uibModal, errorHandler, PreciseApi, Pages, Crafts) {
		var $ctrl = this;
		
		$ctrl.createCraft = createCraft;
		$ctrl.editCraft = editCraft;
		$ctrl.deleteCraft = deleteCraft;
		
		$ctrl.$onChanges = $onChanges;
		
		var deleteErrorHandler = errorHandler.wrapIf(PreciseApi.isHttpConflict, {
			title: 'Cannot delete craft',
			message: 'There are task definitions referencing this craft'
		});
		
		function $onChanges(changes) {
			if (changes.model) {
				loadCrafts();
			}
		}
		
		function loadCrafts() {
			$ctrl.model.getCrafts()
				.then(setCrafts, errorHandler.handle);
		}
		
		function setCrafts(crafts) {
			$ctrl.crafts = crafts;
		}
		
		function openModal(resource) {
			$uibModal.open({
				component: 'preciseCreateCraft',
				resolve: { resource: resource }
			}).result.then(loadCrafts);
		}
		
		function editCraft(craft) {
			openModal(function () {
				return Crafts.existingResource($ctrl.model, _.clone(craft));
			});
		}
		
		function createCraft() {
			openModal(function () {
				return Crafts.newResource($ctrl.model);
			});
		}
		
		function deleteCraft(craft) {
			Crafts
				.existingResource($ctrl.model, craft)
				.then(function (resource) {
					return resource.delete();
				})
				.then(loadCrafts, deleteErrorHandler.handle);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-crafts.html',
		controller: SingleModelCraftsController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
