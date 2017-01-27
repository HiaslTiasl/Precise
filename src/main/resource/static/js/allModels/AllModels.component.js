/**
 * Component for the list of all models.
 * @module "allModels/AllModelsComponent"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsController.$inject = ['$scope', '$q', '$uibModal', 'errorHandler', 'PreciseApi', 'AllModels', 'Models', 'MDLFiles'];
	
	/**
	 * Controller for all models view.
	 * @constructor
	 */
	function AllModelsController($scope, $q, $uibModal, errorHandler, PreciseApi, AllModels, Models, MDLFiles) {
		
		var $ctrl = this;
		
		$ctrl.refreshModels = refreshModels;
		$ctrl.getMDLFileURI = getMDLFileURI;
		$ctrl.getCSVFileURI = AllModels.getCSVFileURI;
		$ctrl.createModel = createModel;
		$ctrl.editModel = editModel;
		$ctrl.importFile = importFile;
		$ctrl.duplicateModel = duplicateModel;
		$ctrl.deleteModel = deleteModel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			// Load initial list of models
			refreshModels();
		}
		
		/** Sets the given models as the list of those currently displayed. */
		function setModels(models) {
			$ctrl.models = models;
		}
		
		/** Loads the list of all models from the server and display them. */
		function refreshModels() {
			AllModels.clearCache();
			return AllModels.getModels().then(setModels, errorHandler.handle);
		}

		/** Returns the URI to the MDL file of the given model. */
		function getMDLFileURI(model) {
			return MDLFiles.base.getModelUrl(model);
		}
		
		/** Opens the given model resource in a modal dialog. */
		function openModal(resource) {
			$uibModal.open({
				component: 'preciseCreateModel',
				resolve: { resource: resource }
			}).result.then(refreshModels);
		}
		
		/** Opens a modal dialog for creating a new model. */
		function createModel() {
			openModal(function () {
				return Models.newResource();
			});
		}
		
		/** Opens a modal dialog for editing the given model. */
		function editModel(model) {
			openModal(function () {
				return Models.existingResource(_.clone(model));
			});
		}

		/** Imports the given file. */
		function importFile(file) {
			return file && AllModels.importFile(file)
				.then(refreshModels, errorHandler.handle);
		}
		
		/** Duplicates the given model. */
		function duplicateModel(model) {
			return AllModels.duplicateModel(model)
				.then(refreshModels, errorHandler.handle);
		}
		
		/** Deletes the given model. */
		function deleteModel(model) {
			// Ask confirmation:
			// Even if undo will be implemented in the future, it will only support undoing
			// changes *in* a model, but not the deletion of models.
			var permission = PreciseApi.asyncConfirm([
				'Are you sure you want to delete model ' + model.name + '?',
				'It cannot be undone afterwards.',
			].join('\n'));
			
			return permission.then(function () {
				// N.B. while it is possible to handle the success case outside in a chained
				// .then call, the same is not possible for handling the failure case, because
				// it would also be called if the user rejects the deletion.
				return AllModels.deleteModel(model)
					.then(refreshModels, errorHandler.handle);
			});
		}
	}
	
	return {
		templateUrl: 'js/allModels/allModels.html',
		controller: AllModelsController,
		controllerAs: '$ctrl'
	};
	
});