define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelConfigController.$inject = ['$http', '$state', '$uibModal', 'errorHandler', 'Pages', 'Files', 'MDLFiles', 'AllModels'];
	
	function SingleModelConfigController($http, $state, $uibModal, errorHandler, Pages, Files, MDLFiles, AllModels) {
		
		var $ctrl = this;
		
		$ctrl.getFileURL = getFileURL;
		$ctrl.getFileName = getFileName;
		$ctrl.importConfig = importConfig;
		$ctrl.clearConfig = clearConfig;

		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model)
				loadPhases();
		}
		
		function getFileURL(model) {
			return MDLFiles.urlToModel(model, MDLFiles.CONFIG_PATH);
		}
		
		function getFileName(model) {
			return MDLFiles.fileNameOf(model.name + ' (config)');
		}
		
		function loadPhases() {
			$ctrl.model.getPhases({ projection: 'expandedPhase' })
				.then(setPhases, errorHandler.handle);
		}
		
		function setPhases(phases) {
			$ctrl.phases = phases;
		}
		
		function importConfig() {
			$uibModal.open({
				component: 'preciseImportModel',
				resolve: {
					model: _.constant($ctrl.model),
					title: _.constant('Configuration'),
					subPath: _.constant(MDLFiles.CONFIG_PATH)
				}
			}).result.then($ctrl.reload);
		}
		
		function clearConfig() {
			MDLFiles.clearConfig($ctrl.model.data)
				.then($ctrl.reload, errorHandler.handle);
		}
		
	}
	
	
	return {
		templateUrl: 'js/singleModel/singleModel-config.html',
		controller: SingleModelConfigController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			reload: '&'
		}
	}
	
});
