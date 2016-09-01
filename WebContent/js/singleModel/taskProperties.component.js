define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['taskProperties'];
	
	function TaskPropertiesController(taskProperties) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.addPattern = addPattern;
		$ctrl.setGlobalExclusiveness = setGlobalExclusiveness;
		$ctrl.setExclusiveness = setExclusiveness;
		$ctrl.showExclusiveness = showExclusiveness;
		$ctrl.showOrder = showOrder;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.updateTask = updateTask;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		$ctrl.orderTypes = taskProperties.getOrderTypes();
		
		$ctrl.$onChanges = $onChanges;
		
		var taskResourceService;
		
		var getAttrName = _.property('name'),
			exclParts,
			orderParts;
		
		function $onChanges(changesObj) {
			if (changesObj['resource']) {				
				taskResourceService = taskProperties.ofResource($ctrl.resource);
				taskResourceService.getData().then(function (data) {
					$ctrl.data = data;
				});
			}
		}
		
		function setGlobalExclusiveness() {
			if ($ctrl.data.globalExclusiveness)
				util.limitArray($ctrl.data.exclusiveness, 0);
		}
		
		function setExclusiveness() {
			$ctrl.data.globalExclusiveness = $ctrl.data.exclusiveness.length > 0;
		}
		
		function showExclusiveness() {
			if (!$ctrl.data)
				return undefined;
			if (!exclParts)
				exclParts = [];
			util.mapInto(exclParts, $ctrl.data.exclusiveness, getAttrName);
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
			if (!$ctrl.data)
				return undefined;
			if (!orderParts)
				orderParts = [];
			util.limitArray(orderParts, 0);
			$ctrl.data.orderSpecifications.forEach(function (order) {
				var str = showOrderPart(order);
				if (str != null)
					orderParts.push(str);
			});
			return orderParts.length ? orderParts.join(', ') : '(no order)';
		}
		
		function updatePattern(pattern, patternNum, attr, newValue) {
			return taskResourceService.updatePattern(pattern, attr, newValue).then(function (checkedPattern) {
				// Copy all properties from the checked pattern to the current one.
				// N.B: In principle we could also just replace the whole pattern at once,
				// but SmartTable does not notice that and the view is not updated.
				// See https://github.com/lorenzofox3/Smart-Table/issues/205
				_.assign($ctrl.data.locationPatterns[patternNum], checkedPattern.original());
			});
		}
		
		function addPattern() {
			return taskResourceService.globalPattern().then(function (checkedPattern) {
				$ctrl.data.locationPatterns.push(checkedPattern.original());
			});
		}
		
		function removePattern(index) {
			$ctrl.data.locationPatterns.splice(index, 1);
		}
		
		function updateTask() {
			return taskResourceService.updateTask($ctrl.data)
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
			return !taskProperties.isAssignableTo(orderType, attribute);
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