define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['PreciseApi', 'Tasks', 'Scopes', 'Phases', 'Pages'];
	
	function TaskPropertiesController(PreciseApi, Tasks, Scopes, Phases, Pages) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.updateExlusivenessType = updateExlusivenessType;
		$ctrl.addPattern = addPattern;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.sendTask = sendTask;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		
		$ctrl.scopeTypes = [
			Scopes.Types.NONE,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		$ctrl.orderTypes = [
			Tasks.OrderTypes.NONE,
			Tasks.OrderTypes.PARALLEL,
			Tasks.OrderTypes.ASCENDING,
			Tasks.OrderTypes.DESCENDING
		];

		$ctrl.$onChanges = $onChanges;
		
		function $onChanges() {
			if ($ctrl.resource) {
				$ctrl.exclusiveness = Scopes.toLocalRepresentation($ctrl.resource.data.exclusiveness);
			}
		}
		
		function updateExlusivenessType() {
			$ctrl.exclusiveness.type = _.some($ctrl.exclusiveness.attributes)
				? Scopes.Types.ATTRIBUTES
				: Scopes.Types.GLOBAL;
		}
		
		function updatePattern(pattern, patternNum, attr, newValue) {
			return $ctrl.resource.updatePattern(pattern, attr, newValue).then(function (checkedPattern) {
				// Copy all properties from the checked pattern to the current one.
				// N.B: In principle we could also just replace the whole pattern at once,
				// but SmartTable does not notice that and the view is not updated.
				// See https://github.com/lorenzofox3/Smart-Table/issues/205
				_.assign($ctrl.resource.data.locationPatterns[patternNum], checkedPattern);
			});
		}
		
		function addPattern() {
			return $ctrl.resource.globalPattern().then(function (checkedPattern) {
				$ctrl.resource.data.locationPatterns.push(checkedPattern);
			});
		}
		
		function removePattern(index) {
			$ctrl.resource.data.locationPatterns.splice(index, 1);
		}
		
		function sendTask() {
			var attributes = $ctrl.resource.data.type.phase.attributes,
				exclusiveness = Scopes.fromLocalRepresentation($ctrl.exclusiveness, attributes);
			$ctrl.resource.data.exclusiveness = exclusiveness;
			return $ctrl.resource.send('expandedTask')
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, function (reason) {
					alert(PreciseApi.toErrorMessage(reason));
				});
		}

		function isDisabledPatternEntry(patternEntry) {
			return _.get(patternEntry, ['allowedValues', 'length'], 0) <= 1;
		}
		
		function isDisabledOrderType(orderType, attribute) {
			return !Tasks.isAssignableTo(orderType, attribute);
		}
		
		function cancel() {
			$ctrl.cancelled();
		}
	}
	
	return {
		templateUrl: 'js/singleModel/taskProperties.html',
		controller: TaskPropertiesController,
		controllerAs: '$ctrl',
		bindings: {
			resource: '<',
			done: '&',
			cancelled: '&'
		}
	};
	
});