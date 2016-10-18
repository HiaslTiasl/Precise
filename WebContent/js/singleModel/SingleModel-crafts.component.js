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
		
		function createCraft() {
			var modalInstance = $uibModal.open({
				component: 'preciseCreateCraft',
				resolve: {
					model: _.constant($ctrl.model)
				}
			}).result.then(loadCrafts);
		}
		
		function deleteCraft(craft) {
			Crafts
				.existingResource($ctrl.model, craft)
				.then(function (resource) {
					resource.delete();
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
