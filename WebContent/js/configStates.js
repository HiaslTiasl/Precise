define([
], function (
) {
	'use strict';
	
	resolveSingleModel.$inject = ['$transition$', 'SingleModel'];
	
	function resolveSingleModel($transition$, SingleModel) {
		return SingleModel.findByName($transition$.params().name);
	}
	
	checkDiagramEditable.$inject = ['$transition$', 'model'];
	
	function checkDiagramEditable($transition$, model) {
		return model.data.diagramInfo.editable
			|| $transition$.router.stateService.target('singleModel.config', $transition$.params());
	}
	
	clearCache.$inject = ['$transition$', 'SingleModel'];
	
	function clearCache($transition$, SingleModel) {
		SingleModel.cache['delete']($transition$.params().name);
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
	
	configStates.$inject = ['$stateProvider', '$urlRouterProvider', '$transitionsProvider'];
	
	function configStates($stateProvider, $urlRouterProvider, $transitionsProvider) {
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
				onEnter: checkDiagramEditable,
				data: {
					title: 'Diagram'
				}
				//onExit: onExitSingleModel,
			});
		
		$urlRouterProvider.otherwise('/models');
		
		$transitionsProvider.onError({
			entering: 'singleModel'
		}, function (trans) {
			if (trans.error().statusCode == 404)
				trans.router.stateService.go('allModels')
		});
	}
	
	return configStates;
});