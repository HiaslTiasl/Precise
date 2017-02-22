/**
 * Angular component for dependency properties.
 * @module "singleModel/dependencyProperties.component"
 */
define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependencyPropertiesController.$inject = ['$q', 'errorHandler', 'PreciseApi', 'Scopes', 'Tasks'];
	
	/**
	 * Controller constructor-
	 * @constructor
	 */
	function DependencyPropertiesController($q, errorHandler, PreciseApi, Scopes, Tasks) {
		var $ctrl = this;
		
		$ctrl.sendDependency = sendDependency;
		$ctrl.isDisabledScopeType = isDisabledScopeType;
		$ctrl.updateScopeType = updateScopeType;
		$ctrl.updateScopeAttributes = updateScopeAttributes;
		$ctrl.toggleCollapsed = toggleCollapsed;
		
		/** Scope types in display order. */
		$ctrl.scopeTypes = [
            Scopes.Types.UNIT,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		/** Returns the phase name of a given task. */
		var getTaskPhaseName = _.property(['activity', 'phase', 'name']);
		
		/** Indicates which of the fieldsets are collapsed. */
		$ctrl.collapsed = {
			scope: false
		};
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.resource) {
				// Update scope on changed resource
				var data = $ctrl.resource.data;
				$ctrl.scope = Scopes.toLocalRepresentation(data.scope);
			}
		}
		
		/** Toggle collapsing of the given fieldset. */
		function toggleCollapsed(fieldset) {
			$ctrl.collapsed[fieldset] = !$ctrl.collapsed[fieldset];
		}
		
		/** Indicates whether the given scope type is currently disabled. */
		function isDisabledScopeType(scopeType) {
			return scopeType === Scopes.Types.UNIT && !canHaveUnitScope();
		}
		
		/** Indicates whether the UNIT scope can be used for this dependency. */
		function canHaveUnitScope() {
			var data = $ctrl.resource.data;
			return !data.source || !data.target
				|| getTaskPhaseName(data.source) === getTaskPhaseName(data.target);
		}
		
		/** Updates the type of the scope according to attributes and compatibility of source and target task. */
		function updateScopeType() {
			Scopes.updateType($ctrl.scope, $ctrl.resource.data.attributes);
			if (isDisabledScopeType($ctrl.scope.type))
				$ctrl.scope.type = Scopes.Types.ATTRIBUTES;
		}

		/** Updates attributes of the scope according to its type. */
		function updateScopeAttributes() {
			Scopes.updateAttributes($ctrl.scope, $ctrl.resource.data.attributes);
		}
		
		/** Sends the dependency to the server to apply the changes. */
		function sendDependency() {
			$ctrl.resource.data.scope = Scopes.fromLocalRepresentation($ctrl.scope, $ctrl.resource.data.attributes);
			return $ctrl.resource.send('dependencySummary')
				.then(function (result) {
					// Call outer change handler
					$ctrl.done({ $result: result });
				}, errorHandler.handle);
		}

	}
	
	return {
		templateUrl: 'js/singleModel/DependencyProperties.html',
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