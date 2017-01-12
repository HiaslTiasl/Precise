define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelCraftsController.$inject = ['$uibModal', 'Pages', 'Crafts'];
	
	function SingleModelCraftsController($uibModal, Pages, Crafts) {
		var $ctrl = this;
		
		$ctrl.createCraft = createCraft;
		$ctrl.editCraft = editCraft;
		$ctrl.deleteCraft = deleteCraft;
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model) {
				loadCrafts();
			}
		}
		
		function loadCrafts() {
			$ctrl.model.getCrafts()
				.then(setCrafts, function (err) {
					console.log(err);
				});
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
				.then(loadCrafts);
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
