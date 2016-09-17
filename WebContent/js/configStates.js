define([
], function (
) {
	'use strict';
	
	resolveAllModels.$inject = ['AllModels'];
	
	function resolveAllModels(AllModels) {
		return AllModels.getModels();
	}
	
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
				templateUrl: 'js/allModels/allModels.html',
				controller: 'AllModelsController',
				controllerAs: '$ctrl',
				resolve: {
					'models': resolveAllModels
				}
			})
			.state('singleModel', {
				url: '/models/:name',
				templateUrl: 'js/singleModel/singleModel.html',
				controller: 'SingleModelController',
				controllerAs: '$ctrl',
				abstract: true,
				resolve: {
					'model': resolveSingleModel
				},
				onExit: clearCache
			})
			.state('singleModel.building', {
				url: '/building',
				templateUrl: 'js/singleModel/singleModel-building.html',
				controller: 'SingleModelBuildingController',
				controllerAs: '$ctrl',
				data: {
					title: 'Building'
				},
				resolve: {
					'models': resolveAllModels
				}
			})
			.state('singleModel.phases', {
				url: '/phases',
				templateUrl: 'js/singleModel/singleModel-phases.html',
				controller: 'SingleModelPhasesController',
				controllerAs: '$ctrl',
				data: {
					title: 'Phases'
				},
				resolve: {
					'phases': resolvePhases
				}
			})
			.state('singleModel.taskTypes', {
				url: '/tasks',
				templateUrl: 'js/singleModel/singleModel-taskTypes.html',
				controller: 'SingleModelTaskTypesController',
				controllerAs: '$ctrl',
				data: {
					title: 'Tasks'
				},
				resolve: {
					'taskTypes': resolveTaskTypes
				}
			})
			.state('singleModel.diagram', {
				url: '/diagram',
				templateUrl: 'js/singleModel/singleModel-diagram.html',
				controller: 'SingleModelDiagramController',
				controllerAs: '$ctrl',
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