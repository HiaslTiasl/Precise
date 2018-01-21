/**
 * Angular component for the crafts view of the configuration.
 * @module "singleModel/SingleModel-crafts.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	CraftsController.$inject = ['$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'Crafts'];
	
	/**
	 * Controller constructor.
	 * @controller
	 */
	function CraftsController($uibModal, errorHandler, PreciseApi, Pages, Crafts) {
		var $ctrl = this;
		
		$ctrl.createCraft = createCraft;
		$ctrl.editCraft = editCraft;
		$ctrl.deleteCraft = deleteCraft;
		
		$ctrl.$onChanges = $onChanges;
		
		/** Specialized error handler for conflicts on deletion. */
		var deleteErrorHandler = errorHandler.wrapIf(PreciseApi.isHttpConflict, {
			title: 'Cannot delete craft',
			message: 'There are activities referencing this craft'
		});
		
		function $onChanges(changes) {
			if (changes.model) {
				loadCrafts();
			}
		}
		
		/** Loads the crafts of the model. */
		function loadCrafts() {
			$ctrl.model.getCrafts()
				.then(setCrafts, errorHandler.handle);
		}
		
		/** Sets the given crafts. */
		function setCrafts(crafts) {
			$ctrl.crafts = crafts;
		}
		
		/** Opens the given craft resource in a modal dialog. */
		function openModal(resource) {
			$uibModal.open({
				component: 'craftsDialog',
				resolve: { resource: resource }
			}).result.then(loadCrafts);
		}
		
		/** Opens a modal dialog for creating a new craft. */
		function createCraft() {
			openModal(function () {
				return Crafts.newResource($ctrl.model);
			});
		}
		
		/** Opens a modal dialog for editing the given craft. */
		function editCraft(craft) {
			openModal(function () {
				return Crafts.existingResource($ctrl.model, _.clone(craft));
			});
		}
		
		/** Deletes the given craft on the server. */
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
		templateUrl: 'js/singleModel/Crafts.html',
		controller: CraftsController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
