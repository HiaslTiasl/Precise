define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependencyPropertiesController.$inject = [];
	
	function DependencyPropertiesController() {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.sendDependency = sendDependency;
		$ctrl.showScope = showScope;
		$ctrl.globalScopeChanged = globalScopeChanged;
		$ctrl.scopeChanged = scopeChanged;
		
		$ctrl.$onChanges = $onChanges;
		
		var getAttrName = _.property('name'),
			scopeParts;
		
		function $onChanges(changesObj) {
			if (changesObj['resource']) {				
				// Nothing to do here
			}
		}
		
		function showScope() {
			if (!$ctrl.resource.data)
				return undefined;
			if (!scopeParts)
				scopeParts = [];
			util.mapInto(scopeParts, $ctrl.resource.data.scope, getAttrName);
			return scopeParts.length ? scopeParts.join(', ') : '(no attributes)';
		}
		
		function globalScopeChanged() {
			if ($ctrl.resource.data.globalScope)
				util.limitArray($ctrl.resource.data.scope, 0);
		}
		
		function scopeChanged() {
			$ctrl.resource.data.globalScope = $ctrl.resource.data.scope.length > 0;
		}
		
		function sendDependency() {
			return $ctrl.resource.send('dependencySummary')
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, function (reason) {
					alert(preciseApi.extractErrorMessage(reason));
				});
		}

		function cancel() {
			$ctrl.cancelled();
		}
	}
	
	return {
		templateUrl: 'js/singleModel/dependencyProperties.html',
		controller: DependencyPropertiesController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			mode: '<',
			resource: '<',
			done: '&',
			cancelled: '&'
		}
	};
	
});