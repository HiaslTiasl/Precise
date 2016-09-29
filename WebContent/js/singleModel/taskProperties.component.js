define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['Tasks', 'Phases', 'Pages'];
	
	function TaskPropertiesController(Tasks, Phases, Pages) {
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
		$ctrl.orderTypes = Tasks.getOrderTypes();
		
		$ctrl.$onChanges = $onChanges;
		
		var getAttrName = _.property('name'),
			exclParts,
			orderParts;
		
		function $onChanges(changesObj) {
			if (changesObj.resource && !$ctrl.resource.exists)
				loadPhases();
		}
		
		function loadPhases() {
			$ctrl.resource.model.getPhases()
				.then(Pages.collectRemaining)
				.then(function (phases) {
					$ctrl.phases = phases;
				});
		}
		
		function phaseChanged() {
			// Reset old list of task types first so they cannot be selected.
			$ctrl.taskTypes = null;
			Phases.existingResource($ctrl.model, $ctrl.phase)
				.then(function (resource) {
					return resource.getTaskTypes();
				})
				.then(Pages.collectRemaining)
				.then(function (taskTypes) {
					$ctrl.taskTypes = taskTypes;
				});
		}
		
		function globalExclusivenessChanged() {
			if ($ctrl.resource.data.globalExclusiveness)
				util.limitArray($ctrl.resource.data.exclusiveness, 0);
		}
		
		function exclusivenessChanged() {
			$ctrl.resource.data.globalExclusiveness = $ctrl.resource.data.exclusiveness.length > 0;
		}
		
		function showExclusiveness() {
			if (!$ctrl.resource.data)
				return undefined;
			if (!exclParts)
				exclParts = [];
			util.mapInto(exclParts, $ctrl.resource.data.exclusiveness, getAttrName);
			return !exclParts.length ? null : exclParts.join(', ');
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
			if (!$ctrl.resource.data)
				return undefined;
			if (!orderParts)
				orderParts = [];
			util.limitArray(orderParts, 0);
			$ctrl.resource.data.orderSpecifications.forEach(function (order) {
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
					alert(preciseApi.toErrorMessage(reason));
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