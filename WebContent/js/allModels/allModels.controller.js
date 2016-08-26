define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsController.$inject = ['$scope', '$q', 'preciseApi', 'allModels', 'models'];
	
	function AllModelsController($scope, $q, preciseApi, allModels, models) {
		
		var $ctrl = this;
		
		$ctrl.models = models;
		
		$ctrl.refreshModels = refreshModels;
		$ctrl.getFileName = getFileName;
		$ctrl.getFileURI = getFileURI;
		$ctrl.createModel = createModel;
		$ctrl.importFile = importFile;
		$ctrl.renameModel = renameModel;
		$ctrl.duplicateModel = duplicateModel;
		$ctrl.deleteModel = deleteModel;
		
		function setModels(models) {
			$ctrl.models = models;
		}
		
		function getFileName(model) {
			return model.name + '.mdl';
		}
		
		function getFileURI(model) {
			return '/files/' + getFileName(model);
		}
		
		function refreshModels() {
			return allModels.getModels().then(setModels);
		}
		
		function createModel(model) {
			
		}
		
		function importFile(file) {
			$ctrl.fileErrorMsg = null;
			return file && allModels.importFile(file)
				.then(refreshModels, function (errReason) {
					$ctrl.fileErrorMsg = preciseApi.extractErrorMessage(errReason);
				});
		}
		
		function renameModel(model, newName) {
			return allModels.renameModel(model, newName)
				['catch'](preciseApi.mapReason(preciseApi.extractErrorMessage));
		}
		
		function duplicateModel(model) {
			
		}
		
		function deleteModel(model) {
			return preciseApi.asyncConfirm([
				'Are you sure you want to delete model ' + model.name + '?',
				'It cannot be undone afterwards.',
			].join('\n'))
			.then(function () {
				return allModels.deleteModel(model)
					.then(refreshModels, function (reason) {
						preciseApi.asyncAlert(preciseApi.extractErrorMessage(reason))
					});
			});
		}
	}
	
	return AllModelsController;
});