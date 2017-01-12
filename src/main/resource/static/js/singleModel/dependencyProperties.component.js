define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependencyPropertiesController.$inject = ['$q', 'errorHandler', 'PreciseApi', 'Scopes', 'Tasks'];
	
	function DependencyPropertiesController($q, errorHandler, PreciseApi, Scopes, Tasks) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.sendDependency = sendDependency;
		$ctrl.isDisabledScopeType = isDisabledScopeType;
		$ctrl.updateScopeType = updateScopeType;
		$ctrl.updateScopeAttributes = updateScopeAttributes;
		$ctrl.toggleCollapsed = toggleCollapsed;
		
		$ctrl.scopeTypes = [
            Scopes.Types.UNIT,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		var getTaskPhaseName = _.property(['type', 'phase', 'name']);
		
		$ctrl.collapsed = {
			scope: false
		};
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.resource) {
				var data = $ctrl.resource.data;
				$ctrl.scope = Scopes.toLocalRepresentation(data.scope);
			}
		}
		
		function toggleCollapsed(fieldset) {
			$ctrl.collapsed[fieldset] = !$ctrl.collapsed[fieldset];
		}
		
		function isDisabledScopeType(scopeType) {
			return scopeType === Scopes.Types.UNIT && !canHaveUnitScope();
		}
		
		function canHaveUnitScope() {
			var data = $ctrl.resource.data;
			return !data.source || !data.target
				|| getTaskPhaseName(data.source) === getTaskPhaseName(data.target);
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
				}, errorHandler.handle);
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