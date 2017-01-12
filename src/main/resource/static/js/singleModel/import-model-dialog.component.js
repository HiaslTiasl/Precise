define([], function () {
	'use strict';
	
	SingleModelConfigImportDialogController.$inject = ['$http', 'errorHandler', 'PreciseApi', 'Files', 'MDLFiles', 'AllModels']
	
	function SingleModelConfigImportDialogController($http, errorHandler, PreciseApi, Files, MDLFiles, AllModels) {
		
		var $ctrl = this;
		
		$ctrl.fromChanged = fromChanged;
		$ctrl.modelChanged = modelChanged;

		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.from = 'file';
		
		$ctrl.$onInit = $onInit;
		
		var modelsPromise;
		
		function $onInit() {
			$ctrl.title = $ctrl.resolve.title;
			$ctrl.subPath = $ctrl.resolve.subPath;
		}
		
		function fromChanged() {
			if ($ctrl.from === 'model')
				loadModels();
		}
		
		function canSelectModel(model) {
			return model.name !== $ctrl.resolve.model.data.name;
		}
		
		function filterSelectableModels(models) {
			return models.filter(canSelectModel);
		}
		
		function loadModels() {
			return modelsPromise || (modelsPromise = AllModels.getModels()
				.then(filterSelectableModels)
				.then(function (models) {
					return $ctrl.selectableModels = models;
				}, errorHandler.handle));
		}
		
		function modelChanged() {
			if ($ctrl.selectedModel)
				$ctrl.file = null;
		}
		
		function selectFile(file) {
			$ctrl.fileErrorMsg = null;
			if (file)
				$ctrl.selectedModel = null;
		}
		
		function send() {
			return ($ctrl.file
				? sendFile($ctrl.file)
				: chooseModel($ctrl.selectedModel)
			).then($ctrl.modalInstance.close, errorHandler.handle);
		}
		
		function sendFile(file) {
			return Files.newReader()
				.readAsText(file)
				.then(function (text) {
					return MDLFiles.importJSON(MDLFiles.urlToModel($ctrl.resolve.model.data, $ctrl.subPath), text);
				})
				['catch'](PreciseApi.mapReason(function (reason) {
					// TODO: Choose what to do: either this or toast
					$ctrl.fileError = PreciseApi.getErrorText(reason.data);
					return reason;
				}));
		}
		
		function chooseModel(data) {
			return $http({
				url: MDLFiles.urlToModel($ctrl.resolve.model.data, $ctrl.subPath),
				method: 'PUT',
				headers: { 'Accept': 'application/json' },
				params: { use: data.name }
			});
		}
		
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/import-model-dialog.html',
		controller: SingleModelConfigImportDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
