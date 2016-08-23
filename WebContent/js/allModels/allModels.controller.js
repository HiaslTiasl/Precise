define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsController.$inject = ['$scope', '$window', '$q', 'preciseApi', 'allModels', 'models'];
	
	function AllModelsController($scope, $window, $q, preciseApi, allModels, models) {
		
		var $ctrl = this;
		
		$ctrl.models = models;
		
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
		
		function createModel(model) {
			
		}
		
		function importFile(file) {
			return allModels.importFile(file)
				.then(allModels.getModels)
				.then(setModels, function (errReason) {
					$window.alert(preciseApi.extractErrorMessage(errReason));
				});
		}
		
		function renameModel(model, newName) {
			return allModels.renameModel(model, newName)
				['catch'](preciseApi.mapReason(preciseApi.extractErrorMessage));
		}
		
		function duplicateModel(model) {
			
		}
		
		function deleteModel(model) {
			
		}
	}
	
	return AllModelsController;
});