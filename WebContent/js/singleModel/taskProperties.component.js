define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['TaskResource'];
	
	function TaskPropertiesController(TaskResource) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.addPattern = addPattern;
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.globalExclusivenessChanged = globalExclusivenessChanged;
		$ctrl.exclusivenessChanged = exclusivenessChanged;
		$ctrl.showExclusiveness = showExclusiveness;
		$ctrl.showOrder = showOrder;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.sendTask = sendTask;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		$ctrl.orderTypes = TaskResource.getOrderTypes();
		
		$ctrl.$onChanges = $onChanges;
		
		var getAttrName = _.property('name'),
			exclParts,
			orderParts;
		
		function $onChanges(changesObj) {
			if ('resource' in changesObj) {
				if (!$ctrl.resource.exists) {
					$ctrl.resource.getPhases().then(function (phases) {
						$ctrl.phases = phases;
					});
				}
			}
		}
		
		function phaseChanged() {
			// Reset old list of task types first so they cannot be selected.
			$ctrl.taskTypes = null;
			$ctrl.resource.getTaskTypes($ctrl.phase).then(function (taskTypes) {
				$ctrl.taskTypes = taskTypes;
			});
		}
		
		function globalExclusivenessChanged() {
			if ($ctrl.resource.task.globalExclusiveness)
				util.limitArray($ctrl.resource.task.exclusiveness, 0);
		}
		
		function exclusivenessChanged() {
			$ctrl.resource.task.globalExclusiveness = $ctrl.resource.task.exclusiveness.length > 0;
		}
		
		function showExclusiveness() {
			if (!$ctrl.resource.task)
				return undefined;
			if (!exclParts)
				exclParts = [];
			util.mapInto(exclParts, $ctrl.resource.task.exclusiveness, getAttrName);
			return exclParts.length ? exclParts.join(', ') : '(no attributes)';
		}
		
		function showOrderPart(order) {
			var attrName = order.attribute.name;
			switch (order.orderType) {
			case 'PARALLEL':
				return '|' + attrName + '|';
			case 'ASCENDING':
				return attrName + '\u2191'; 	// ↑
			case 'DESCENDING':
				return attrName + '\u2193'; 	// ↓
			default:
				return null;
			}
		}
		
		function showOrder() {
			if (!$ctrl.resource.task)
				return undefined;
			if (!orderParts)
				orderParts = [];
			util.limitArray(orderParts, 0);
			$ctrl.resource.task.orderSpecifications.forEach(function (order) {
				var str = showOrderPart(order);
				if (str != null)
					orderParts.push(str);
			});
			return orderParts.length ? orderParts.join(', ') : '(no order)';
		}
		
		function updatePattern(pattern, patternNum, attr, newValue) {
			return $ctrl.resource.updatePattern(pattern, attr, newValue).then(function (checkedPattern) {
				// Copy all properties from the checked pattern to the current one.
				// N.B: In principle we could also just replace the whole pattern at once,
				// but SmartTable does not notice that and the view is not updated.
				// See https://github.com/lorenzofox3/Smart-Table/issues/205
				_.assign($ctrl.resource.task.locationPatterns[patternNum], checkedPattern);
			});
		}
		
		function addPattern() {
			return $ctrl.resource.globalPattern().then(function (checkedPattern) {
				$ctrl.resource.task.locationPatterns.push(checkedPattern);
			});
		}
		
		function removePattern(index) {
			$ctrl.resource.task.locationPatterns.splice(index, 1);
		}
		
		function sendTask() {
			return $ctrl.resource.sendTask()
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, function (reason) {
					alert(preciseApi.extractErrorMessage(reason));
				});
		}

		function isDisabledPatternEntry(patternEntry) {
			return _.get(patternEntry, ['allowedValues', 'length'], 0) <= 1;
		}
		
		function isDisabledOrderType(orderType, attribute) {
			return !TaskResource.isAssignableTo(orderType, attribute);
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