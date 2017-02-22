/**
 * Angular component for the configuration part in the single model view.
 * @module "singleModel/SingleModel-config.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	ConfigController.$inject = ['$http', '$state', '$uibModal', 'errorHandler', 'Pages', 'Files', 'MDLFiles', 'AllModels'];
	
	/**
	 * Controller constructor.
	 * @controller
	 */
	function ConfigController($http, $state, $uibModal, errorHandler, Pages, Files, MDLFiles, AllModels) {
		
		var $ctrl = this;
		
		$ctrl.getFileURL = getFileURL;
		$ctrl.importConfig = importConfig;
		$ctrl.clearConfig = clearConfig;

		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.model)
				loadPhases();
		}
		
		/** Loads the phases of this model. */
		function loadPhases() {
			$ctrl.model.getPhases({ projection: 'expandedPhase' })
				.then(setPhases, errorHandler.handle);
		}
		
		/** Sets the given phases. */
		function setPhases(phases) {
			$ctrl.phases = phases;
		}
		
		/** Returns the URL to the MDL file of the configuration of the given model. */
		function getFileURL(model) {
			return MDLFiles.config.getModelUrl(model);
		}
		
		/** Opens a dialog for importing the configuration of a model into this one. */
		function importConfig() {
			$uibModal.open({
				component: 'ImportModelDialog',
				resolve: {
					model: _.constant($ctrl.model),
					title: _.constant('Configuration'),
					mdlContext: _.constant(MDLFiles.config)
				}
			}).result.then($ctrl.reload);
		}
		
		/** Resets the configuration of this model to an empty one. */
		function clearConfig() {
			MDLFiles.clearConfig($ctrl.model.data)
				.then($ctrl.reload, errorHandler.handle);
		}
		
	}
	
	
	return {
		templateUrl: 'js/singleModel/Config.html',
		controller: ConfigController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			reload: '&'
		}
	}
	
});
