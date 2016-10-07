define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependencyPropertiesController.$inject = ['PreciseApi', 'Scopes'];
	
	function DependencyPropertiesController(PreciseApi, Scopes) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.sendDependency = sendDependency;
		$ctrl.updateScopeType = updateScopeType;
		
		$ctrl.scopeTypes = [
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		$ctrl.$onChanges = $onChanges;
		
		function $onChanges(changes) {
			if (changes.resource) {
				$ctrl.scope = Scopes.toLocalRepresentation($ctrl.resource.data.scope);
			}
		}
		
		var getAttrName = _.property('name'),
			scopeParts;
		
		function updateScopeType() {
			$ctrl.scope.type = _.some($ctrl.scope.attributes)
				? Scopes.Types.ATTRIBUTES
				: Scopes.Types.GLOBAL;
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