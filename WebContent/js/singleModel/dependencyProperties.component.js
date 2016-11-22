define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependencyPropertiesController.$inject = ['$q', 'PreciseApi', 'Scopes', 'Tasks'];
	
	function DependencyPropertiesController($q, PreciseApi, Scopes, Tasks) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.sendDependency = sendDependency;
		$ctrl.isDisabledScopeType = isDisabledScopeType;
		$ctrl.updateScopeType = updateScopeType;
		$ctrl.updateScopeAttributes = updateScopeAttributes;
		
		$ctrl.scopeTypes = [
            Scopes.Types.UNIT,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.resource) {
				var data = $ctrl.resource.data;
				$ctrl.scope = Scopes.toLocalRepresentation(data.scope);
			}
		}
		
		var getAttrName = _.property('name'),
			scopeParts;
		
		function isDisabledScopeType(scopeType) {
			return scopeType === Scopes.Types.UNIT && !canHaveUnitScope();
		}
		
		function canHaveUnitScope() {
			var data = $ctrl.resource.data;
			return !data.source || !data.target
				|| _.get(data.source, ['type', 'phase', 'name']) === _.get(data.target, ['type', 'phase', 'name']);
		}
		
		function updateScopeType() {
			Scopes.updateType($ctrl.scope, $ctrl.resource.data.attributes);
			if ($ctrl.scope.type === Scopes.Types.UNIT && !canHaveUnitScope())
				$ctrl.scope.type = Scopes.Types.ATTRIBUTES;
		}

		function updateScopeAttributes() {
			Scopes.updateAttributes($ctrl.scope, $ctrl.resource.data.attributes);
		}
		
		function sendDependency() {
			$ctrl.resource.data.scope = Scopes.fromLocalRepresentation($ctrl.scope, $ctrl.resource.data.attributes);
			return $ctrl.resource.send('dependencySummary')
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, function (reason) {
					alert(PreciseApi.toErrorMessage(reason));
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
			resource: '<',
			done: '&',
			cancelled: '&'
		}
	};
	
});