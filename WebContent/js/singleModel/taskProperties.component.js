define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['PreciseApi', 'Tasks', 'Phases', 'Pages'];
	
	function TaskPropertiesController(PreciseApi, Tasks, Phases, Pages) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.addPattern = addPattern;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.sendTask = sendTask;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		$ctrl.orderTypes = Tasks.getOrderTypes();
		
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