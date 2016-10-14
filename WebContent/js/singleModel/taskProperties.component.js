define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['PreciseApi', 'Tasks', 'Scopes', 'OrderSpecifications', 'Phases', 'Pages'];
	
	function TaskPropertiesController(PreciseApi, Tasks, Scopes, OrderSpecifications, Phases, Pages) {
		var $ctrl = this;
		
		$ctrl.cancel = cancel;
		$ctrl.updateExlusivenessType = updateExlusivenessType;
		$ctrl.updateExclusivenessAttributes = updateExclusivenessAttributes;
		$ctrl.attrFilterForOrderSpec = attrFilterForOrderSpec;
		$ctrl.selectedOrderSpecAttribute = selectedOrderSpecAttribute
		$ctrl.addOrderSpec = addOrderSpec;
		$ctrl.removeOrderSpec = removeOrderSpec;
		$ctrl.canAddOrderSpec = canAddOrderSpec;
		$ctrl.canMoveDownOrderSpec = canMoveDownOrderSpec;
		$ctrl.canMoveUpOrderSpec = canMoveUpOrderSpec;
		$ctrl.moveUpOrderSpec = moveUpOrderSpec;
		$ctrl.moveDownOrderSpec = moveDownOrderSpec;
		$ctrl.addPattern = addPattern;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.sendTask = sendTask;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		
		$ctrl.scopeTypes = [
			Scopes.Types.UNIT,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		$ctrl.orderTypes = [
			OrderSpecifications.Types.NONE,
			OrderSpecifications.Types.PARALLEL,
			OrderSpecifications.Types.ASCENDING,
			OrderSpecifications.Types.DESCENDING
		];

		$ctrl.$onChanges = $onChanges;
		
		function $onChanges() {
			if ($ctrl.resource) {
				$ctrl.exclusiveness = Scopes.toLocalRepresentation($ctrl.resource.data.exclusiveness);
				$ctrl.order = OrderSpecifications.toLocalRepresentation($ctrl.resource.data.orderSpecifications);
			}
		}
		
		function updateExlusivenessType() {
			Scopes.updateType($ctrl.exclusiveness, $ctrl.resource.data.type.phase.attributes.length);
		}
		
		function updateExclusivenessAttributes() {
			Scopes.updateAttributes($ctrl.exclusiveness);
		}
		
		function attrFilterForOrderSpec(os) {
			return function (a) {
				return a.name === os.attribute.name || canSelectForOrderSpec(a);
			}
		}
		
		function canSelectForOrderSpec(a) {
			return !$ctrl.order.attrs[a.name];
		}
		
		function selectedOrderSpecAttribute(newAttr, oldAttr) {
			if (oldAttr)
				$ctrl.order.attrs[oldAttr.name] = false;
			$ctrl.order.attrs[newAttr.name] = true;
		}
		
		function addOrderSpec() {
			var attr = _.find($ctrl.resource.data.type.phase.attributes, canSelectForOrderSpec);
			if (attr)
				selectedOrderSpecAttribute(attr);
			$ctrl.order.specs.push({
				orderType: OrderSpecifications.Types.NONE,
				attribute: attr
			});
		}
		
		function moveUpOrderSpec(index) {
			util.swap($ctrl.order.specs, index, index - 1);
		}
		
		function moveDownOrderSpec(index) {
			util.swap($ctrl.order.specs, index, index + 1);
		}
		
		function removeOrderSpec(index) {
			var removed = $ctrl.order.specs.splice(index, 1);
			$ctrl.order.attrs[removed[0].attribute.name] = false;
		}
		
		function canAddOrderSpec() {
			return _.size($ctrl.order.specs) < _.size($ctrl.resource.data.type.phase.attributes);
		}
		
		function canMoveUpOrderSpec(index) {
			return index > 0;						
		}
		
		function canMoveDownOrderSpec(index) {
			return index < _.size($ctrl.order.specs) - 1;			
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
				exclusiveness = Scopes.fromLocalRepresentation($ctrl.exclusiveness, attributes),
				orderSpecifications = OrderSpecifications.fromLocalRepresentation($ctrl.order, attributes);
			$ctrl.resource.data.exclusiveness = exclusiveness;
			$ctrl.resource.data.orderSpecifications = orderSpecifications;
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
			return !OrderSpecifications.isAssignableTo(orderType, attribute);
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