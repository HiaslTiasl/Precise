define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	resolveSingleModelWithPartInfos.$inject = ['$transition$', 'Models'];
	
	function resolveSingleModelWithPartInfos($transition$, Models) {
		return Models.findByNameWithPartInfos($transition$.params().name);
	}
	
	checkDiagramEditable.$inject = ['$transition$', 'Models'];
	
	function checkDiagramEditable($transition$, Models) {
		return Models.findByNameWithPartInfos($transition$.params().name)
			.then(function (model) {
				return model.data.diagramInfo.editable
					|| $transition$.router.stateService.target('singleModel.config', $transition$.params());
			});
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
				resolve: [{
					token: 'model',
					resolveFn: resolveSingleModelWithPartInfos,
					deps: resolveSingleModelWithPartInfos.$inject
				}],
				resolvePolicy: {
					'model': {
						when: 'EAGER'	
					}
				}
			})
			.state('singleModel.config', {
				url: '/config',
				template: '<precise-config model="$ctrl.model" reload="$ctrl.reload()"></precise-config',
				data: {
					title: 'Configuration'
				}
			})
			.state('singleModel.diagram', {
				url: '/diagram',
				template: '<precise-diagram model="$ctrl.model" reload="$ctrl.reload()"></precise-diagram>',
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
			if (_.get(trans.error(), ['httpResponse', 'status']) === 404)
				trans.router.stateService.go('allModels')
		});
	}
	
	return configStates;
});