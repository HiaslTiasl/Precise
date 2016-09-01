define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	

	
	DependencyPropertiesController.$inject = ['dependencyProperties'];
	
	function DependencyPropertiesController(dependencyProperties) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.updateDependency = updateDependency;
		$ctrl.showScope = showScope;
		$ctrl.setGlobalScope = setGlobalScope;
		$ctrl.setScope = setScope;
		
		$ctrl.$onChanges = $onChanges;
		
		var dependencyResourceService;
		
		var getAttrName = _.property('name'),
			scopeParts;
		
		function $onChanges(changesObj) {
			if (changesObj['resource']) {				
				dependencyResourceService = dependencyProperties.ofResource($ctrl.resource);
				dependencyResourceService.getData().then(function (data) {
					$ctrl.data = data;
				});
			}
		}
		
		function showScope() {
			if (!$ctrl.data)
				return undefined;
			if (!scopeParts)
				scopeParts = [];
			util.mapInto(scopeParts, $ctrl.data.scope, getAttrName);
			return scopeParts.length ? scopeParts.join(', ') : '(no attributes)';
		}
		
		function setGlobalScope() {
			if ($ctrl.data.globalScope)
				util.limitArray($ctrl.data.scope, 0);
		}
		
		function setScope() {
			$ctrl.data.globalScope = $ctrl.data.scope.length > 0;
		}
		
		function updateDependency() {
			return dependencyResourceService.updateDependency($ctrl.data)
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, function (reason) {
					alert(preciseApi.extractErrorMessage(reason));
				});
		}

		function cancel() {
			$ctrl.onCancel();
		}
	}
	
	return {
		templateUrl: 'js/singleModel/dependencyProperties.html',
		controller: DependencyPropertiesController,
		controllerAs: '$ctrl',
		bindings: {
			resource: '<',
			done: '&',
			cancelled: '&'
		}
	};
	
});