define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsController.$inject = ['$scope', '$q', '$uibModal', 'PreciseApi', 'AllModels', 'MDLFiles'];
	
	function AllModelsController($scope, $q, $uibModal, PreciseApi, AllModels, MDLFiles) {
		
		var $ctrl = this;
		
		$ctrl.refreshModels = refreshModels;
		$ctrl.getMDLFileURI = MDLFiles.urlToModel;
		$ctrl.getCSVFileURI = getCSVFileURI;
		$ctrl.createModel = createModel;
		$ctrl.importFile = importFile;
		$ctrl.renameModel = renameModel;
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
			return AllModels.getModels().then(setModels);
		}
		
		function createModel(model) {
			$uibModal.open({
				component: 'preciseCreateModel'
			}).result.then(refreshModels);
		}
		
		function getCSVFileURI(model) {
			return "/files/" + model.name + ".csv";
		}
		
		function importFile(file) {
			$ctrl.fileErrorMsg = null;
			return file && AllModels.importFile(file)
				.then(refreshModels, function (errReason) {
					$ctrl.fileErrorMsg = errReason;
				});
		}
		
		function renameModel(model, newName) {
			return AllModels.renameModel(model, newName)
				['catch'](PreciseApi.mapReason(PreciseApi.toErrorMessage));
		}
		
		function duplicateModel(model) {
			
		}
		
		function deleteModel(model) {
			return PreciseApi.asyncConfirm([
				'Are you sure you want to delete model ' + model.name + '?',
				'It cannot be undone afterwards.',
			].join('\n'))
			.then(function () {
				return AllModels.deleteModel(model)
					.then(refreshModels, function (reason) {
						PreciseApi.asyncAlert(PreciseApi.toErrorMessage(reason));
					});
			});
		}
	}
	
	return {
		templateUrl: 'js/allModels/allModels.html',
		controller: AllModelsController,
		controllerAs: '$ctrl'
	};
	
});