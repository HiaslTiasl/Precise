define([], function () {
	'use strict';
	
	SingleModelConfigImportDialogController.$inject = ['$http', 'Files', 'MDLFiles', 'AllModels']
	
	function SingleModelConfigImportDialogController($http, Files, MDLFiles, AllModels) {
		
		var $ctrl = this;
		
		$ctrl.fromChanged = fromChanged;
		$ctrl.modelChanged = modelChanged;

		$ctrl.sendConfig = sendConfig;
		$ctrl.cancel = cancel;
		
		$ctrl.from = 'file';
		
		var modelsPromise;
		
		function fromChanged() {
			if ($ctrl.from === 'model')
				loadModels();
		}
		
		function canUseConfigOf(model) {
			return model.name !== $ctrl.resolve.model.data.name;
		}
		
		function filterSelectableModels(models) {
			return models.filter(canUseConfigOf);
		}
		
		function loadModels() {
			return modelsPromise || (modelsPromise = AllModels.getModels()
				.then(filterSelectableModels)
				.then(function (models) {
					return $ctrl.selectableModels = models;
				}));
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
		
		function sendConfig() {
			return ($ctrl.file
				? sendFile($ctrl.file)
				: chooseModel($ctrl.selectedModel)
			).then($ctrl.modalInstance.close);
		}
		
		function sendFile(file) {
			return Files.newReader()
				.readAsText(file)
				.then(function (text) {
					return MDLFiles.importJSON(MDLFiles.urlToModel($ctrl.resolve.model.data, true), text);
				});
		}
		
		function chooseModel(data) {
			return $http({
				url: MDLFiles.urlToModel($ctrl.resolve.model.data, true),
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
		templateUrl: 'js/singleModel/singleModel-config.import-dialog.html',
		controller: SingleModelConfigImportDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
