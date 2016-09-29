define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelConfigController.$inject = ['$http', '$state', '$uibModal', 'Pages', 'Files', 'MDLFiles', 'AllModels'];
	
	function SingleModelConfigController($http, $state, $uibModal, Pages, Files, MDLFiles, AllModels) {
		
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
			return MDLFiles.urlToModel(model, true);
		}
		
		function getFileName(model) {
			return MDLFiles.fileNameOf(model.name + ' (config)');
		}
		
		function loadPhases() {
			$ctrl.model.getPhases({ projection: 'expandedPhase' })
				.then(Pages.collectRemaining)
				.then(setPhases);
		}
		
		function setPhases(phases) {
			$ctrl.phases = phases;
		}
		
		function importConfig() {
			$uibModal.open({
				component: 'preciseImportConfig',
				resolve: {
					model: function () {
						return $ctrl.model;
					}
				}
			}).result.then($ctrl.reload);
		}
		
		function clearConfig() {
			MDLFiles.clearConfig($ctrl.model.data)
				.then($ctrl.reload);
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
