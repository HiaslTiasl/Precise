define([
], function (
) {
	'use strict';
	
	resolveSingleModel.$inject = ['$stateParams', '$state', 'SingleModel'];
	
	function resolveSingleModel($stateParams, $state, SingleModel) {
		return SingleModel.findByName($stateParams.name)
			['catch'](function () {
				$state.go('allModels');
			});
	}
	
	resolvePhases.$inject = ['model', 'Pages'];
	
	function resolvePhases(model, Pages) {
		return model.getPhases()
			.then(Pages.collectRemaining);
	}

	resolveTaskTypes.$inject = ['model', 'Pages'];
	
	function resolveTaskTypes(model, Pages) {
		return model.getTaskTypes()
			.then(Pages.collectRemaining);
	}	
	
	checkBuildingConfigured.$inject = ['$stateParams', '$state', 'model'];
	
	function checkBuildingConfigured($stateParams, $state, model) {
		if (!model.data.buildingConfigured)
			$state.go('singleModel.building', $stateParams);
	}
	
	clearCache.$inject = ['$stateParams', 'SingleModel'];
	
	function clearCache($stateParams, SingleModel) {
		SingleModel.cache['delete']($stateParams.name);
	}
	
	onExitSingleModel.$inject = ['$window', '$timeout', '$q', 'model'];
	
	function onExitSingleModel($window, $timeout, $q, model) {
		return $q(function (resolve, reject) {
			$timeout(function () {
				var okToExit = $window.alert('exit model ' + model.name);
				
				if (okToExit)
					resolve();
				else
					reject();
			});
		});
	}
	
	configStates.$inject = ['$stateProvider', '$urlRouterProvider'];
	
	function configStates($stateProvider, $urlRouterProvider) {
		$stateProvider
			.state('allModels', {
				url: '/models',
				component: 'preciseAllModels'
			})
			.state('singleModel', {
				url: '/models/:name',
				component: 'preciseSingleModel',
				abstract: true,
				onExit: clearCache,
				resolve: {
					'model': resolveSingleModel
				}
			})
			.state('singleModel.config', {
				url: '/config',
				component: 'preciseConfig',
				data: {
					title: 'Configuration'
				}
			})
			.state('singleModel.diagram', {
				url: '/diagram',
				component: 'preciseDiagram',
				onEnter: checkBuildingConfigured,
				data: {
					title: 'Diagram'
				}
				//onExit: onExitSingleModel,
			});
		
		$urlRouterProvider.otherwise('/models');
	}
	
	return configStates;
});