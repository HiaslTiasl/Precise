/**
 * Angular component to be opened in a dialog for importing a potentially partial model.
 * @module "singleModel/import-model-dialog.component"
 */
define(function () {
	'use strict';
	
	SingleModelConfigImportDialogController.$inject = ['$http', 'errorHandler', 'PreciseApi', 'Files', 'MDLFiles', 'AllModels']
	
	/**
	 * Controller constructor.
	 * qconstructor
	 */
	function SingleModelConfigImportDialogController($http, errorHandler, PreciseApi, Files, MDLFiles, AllModels) {
		
		var $ctrl = this;
		
		$ctrl.fromChanged = fromChanged;
		$ctrl.modelChanged = modelChanged;

		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.from = 'file';
		
		$ctrl.$onInit = $onInit;
		
		var modelsPromise;		// Promise of models for lazy loading on first selection of model only
		
		function $onInit() {
			$ctrl.title = $ctrl.resolve.title;				// Indicates the part of the model to be imported, i.e. 'Model', 'Configuration', or 'Diagram'
			$ctrl.mdlContext = $ctrl.resolve.mdlContext;	// The context to be used to obtain URLs for a given file or model.
		}
		
		/** The way how to import was changed, so load models if needed. */
		function fromChanged() {
			if ($ctrl.from === 'model')
				loadModels();
		}
		
		/** Indicates whether the given model can be selected to be imported. */
		function canSelectModel(model) {
			// There is no point in importing the current model itself
			return model.name !== $ctrl.resolve.model.data.name;
		}
		
		/** Filters the given models such that only the selectable ones are returned. */
		function filterSelectableModels(models) {
			return models.filter(canSelectModel);
		}
		
		/**
		 * Returns a promise of the models stored on the server which can be selected
		 * for being imported into this model.
		 * When the promise is resolved with a list of models, that list is set in the
		 * controller, and future calls return the same promise without any o
		 */
		function loadModels() {
			// N.B: getModels already caches the result, but with this strategy we als
			// avoid filtering the models multiple times.
			// Also, implementing the caching based on whether $ctrl.selectableModels
			// already exists does not work when loadModels() is called with a short
			// interval in between such that the promise returned from the first call
			// was not resolved yet at the beginning of the second call.
			return modelsPromise || (modelsPromise = AllModels.getModels()
				.then(filterSelectableModels)
				.then(function (models) {
					return $ctrl.selectableModels = models;
				}, errorHandler.handle));
		}
		
		/** A model was selected, so reset the currently selected file. */
		function modelChanged() {
			if ($ctrl.selectedModel)
				$ctrl.file = null;
		}
		
		/** A file was selected, so reset the currently selected model. */
		function selectFile(file) {
			if (file)
				$ctrl.selectedModel = null;
		}
		
		/** Import the currently chosen file or model. */
		function send() {
			return ($ctrl.file
				? sendFile($ctrl.file)
				: chooseModel($ctrl.selectedModel)
			).then($ctrl.modalInstance.close, errorHandler.handle);
		}
		
		/** Import the currently chosen file. */
		function sendFile(file) {
			return Files.newReader()
				.readAsText(file)
				.then(function (text) {
					return MDLFiles.importJSON($ctrl.mdlContext.getModelUrl($ctrl.resolve.model.data), text);
				});
		}
		
		/** Import the currently chosen model. */
		function chooseModel(data) {
			return $http({
				url: $ctrl.mdlContext.getModelUrl($ctrl.resolve.model.data),
				method: 'PUT',
				headers: { 'Accept': 'application/json' },
				params: { use: data.name }
			});
		}
		
		/** Cancel importing and dismiss the dialog. */
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
