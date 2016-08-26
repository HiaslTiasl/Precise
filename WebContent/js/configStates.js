define(function () {
	'use strict';
	
	resolveAllModels.$inject = ['allModels'];
	
	function resolveAllModels(allModels) {
		return allModels.getModels();
	}
	
	resolveSingleModel.$inject = ['$stateParams', '$state', 'singleModel'];
	
	function resolveSingleModel($stateParams, $state, singleModel) {
		return singleModel.findByName($stateParams.name)
			['catch'](function () {
				$state.go('allModels');
			});
	}
	
	onEnterSingleModel.$inject = ['$window', '$timeout', '$q', 'model'];
	
	function onEnterSingleModel($window, $timeout, $q, model) {
		return $q(function (resolve, reject) {
			$timeout(function () {
				var okToEnter = $window.confirm([
					'enter model ' + model.name + ':',
					'building ' + model.buildingConfigured ? 'configured' : 'not configured'
				].join('\n'));
				
				if (okToEnter)
					resolve();
				else
					reject();
			});
		});
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
				controller: 'AllModelsController',
				controllerAs: '$ctrl',
				templateUrl: 'js/allModels/allModels.html',
				resolve: {
					'models': resolveAllModels
				}
			})
			.state('singleModel', {
				url: '/models/:name',
				controller: 'SingleModelController',
				controllerAs: '$ctrl',
				templateUrl: 'js/singleModel/singleModel.html',
				//onEnter: onEnterSingleModel,
				//onExit: onExitSingleModel,
				resolve: {
					'model': resolveSingleModel
				}
			})
			.state('singleModel.building', {
				
			})
			.state('singleModel.taskTypes', {
				
			})
			.state('singleModel.flow', {
				
			});
		
		$urlRouterProvider.otherwise('/models');
	}
	
	return configStates;
});