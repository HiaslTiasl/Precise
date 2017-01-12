define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsController.$inject = ['$scope', '$q', '$uibModal', 'errorHandler', 'PreciseApi', 'AllModels', 'Models', 'MDLFiles'];
	
	function AllModelsController($scope, $q, $uibModal, errorHandler, PreciseApi, AllModels, Models, MDLFiles) {
		
		var $ctrl = this;
		
		$ctrl.refreshModels = refreshModels;
		$ctrl.getMDLFileURI = MDLFiles.urlToModel;
		$ctrl.getCSVFileURI = getCSVFileURI;
		$ctrl.createModel = createModel;
		$ctrl.editModel = editModel;
		$ctrl.importFile = importFile;
		$ctrl.duplicateModel = duplicateModel;
		$ctrl.deleteModel = deleteModel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			refreshModels();
		}
		
		function setModels(models) {
			$ctrl.models = models;
		}
		
		function refreshModels() {
			AllModels.clearCache();
			return AllModels.getModels().then(setModels, errorHandler.handle);
		}
		
		function openModal(resource) {
			$uibModal.open({
				component: 'preciseCreateModel',
				resolve: { resource: resource }
			}).result.then(refreshModels);
		}
		
		function createModel() {
			openModal(function () {
				return Models.newResource();
			});
		}
		
		function editModel(model) {
			openModal(function () {
				return Models.existingResource(_.clone(model));
			});
		}
		
		function getCSVFileURI(model) {
			return "files/" + model.name + ".csv";
		}
		
		function showError(error) {
			$uibModal.open({
				component: 'preciseErrorDialog',
				resolve: {
					errors: function () {
						return Array.isArray(error) ? error : [error];
					} 
				}
			});
		}
		
		function importFile(file) {
			$ctrl.fileErrorMsg = null;
			return file && AllModels.importFile(file)
				.then(refreshModels, errorHandler.handle);
		}
		
		function duplicateModel(model) {
			return AllModels.duplicateModel(model)
				.then(refreshModels, errorHandler.handle);
		}
		
		function deleteModel(model) {
			return PreciseApi.asyncConfirm([
				'Are you sure you want to delete model ' + model.name + '?',
				'It cannot be undone afterwards.',
			].join('\n'))
			.then(function () {
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