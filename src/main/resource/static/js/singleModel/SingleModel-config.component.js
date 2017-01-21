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
		$ctrl.importConfig = importConfig;
		$ctrl.clearConfig = clearConfig;

		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model)
				loadPhases();
		}
		
		function getFileURL(model) {
			return MDLFiles.config.getModelUrl(model);
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
					mdlContext: _.constant(MDLFiles.config)
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
