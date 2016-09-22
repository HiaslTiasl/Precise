define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelBuildingController.$inject = ['$http', '$state', 'Files', 'MDLFiles', 'AllModels', 'SingleModel'];
	
	function SingleModelBuildingController($http, $state, Files, MDLFiles, AllModels, SingleModel) {
		
		var $ctrl = this;
		
		$ctrl.selectFile = selectFile;
		$ctrl.modelChanged = modelChanged;
		$ctrl.clearConfig = clearConfig;
		$ctrl.sendConfig = sendConfig;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		$ctrl.$onChanges = $onChanges;
		
		function $onInit() {
			loadModels();
		}
		
		function $onChanges(changes) {
			if (changes.model) {
				$ctrl.selectedModel = $ctrl.model.data.buildingConfigured ? $ctrl.model.data : null;
				modelChanged();
			}
		}
		
		function loadModels() {
			return AllModels.getModels().then(function (models) {
				$ctrl.models = models;
			});
		}
		
		function showConfigPreview(mdl) {
			$ctrl.config = mdl;
		}
		
		function selectFile(file) {
			$ctrl.fileErrorMsg = null;
			if (file) {
				$ctrl.selectedModel = null;
				Files.newReader()
					.readAsText(file)
					.then(JSON.parse)
					.then(function (json) {
						showConfigPreview(_.pick(json, 'attributes', 'phases', 'taskTypes'));
					});
			}
		}
		
		function modelChanged() {
			if ($ctrl.selectedModel) {
				$ctrl.file = null;
				$http.get(MDLFiles.getConfigFileURI($ctrl.selectedModel)).then(function (response) {
					showConfigPreview(response.data);
				});
			}
		}
		
		function clearConfig() {
			MDLFiles.clearConfig($ctrl.model.data)
				.then(reload);
		}
		
		function sendConfig() {
			MDLFiles.importConfigFile($ctrl.model.data, $ctrl.config)
				.then(reload);
		}
		
		function reload() {
			SingleModel.cache['delete']($ctrl.model.data.name);
			$state.reload();
		}
		
		function cancel() {
			$ctrl.selectedModel = $ctrl.file = null;
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-building.html',
		controller: SingleModelBuildingController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			models: '<'
		}
	};
	
});
