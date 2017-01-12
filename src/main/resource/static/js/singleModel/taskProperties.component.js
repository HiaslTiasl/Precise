define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['$q', '$uibModal', '$anchorScroll', '$timeout', 'errorHandler', 'PreciseApi', 'Tasks', 'TaskTypes', 'Scopes', 'OrderSpecifications', 'Phases', 'Pages'];
	
	function TaskPropertiesController($q, $uibModal, $anchorScroll, $timeout, errorHandler, PreciseApi, Tasks, TaskTypes, Scopes, OrderSpecifications, Phases, Pages) {
		var $ctrl = this;
		
		$ctrl.editTaskDefinition = editTaskDefinition;
		$ctrl.computePitches = computePitches;
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
		$ctrl.toggleCollapsed = toggleCollapsed;
		
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
		
		$ctrl.collapsed = {
			pitches: true,
			exclusiveness: true,
			order: true,
			locations: true
		};

		$ctrl.$onChanges = $onChanges;
		
		var getTaskAttributes = _.property(['type', 'phase', 'attributes']);
		
		function $onChanges() {
			if ($ctrl.resource) {
				$ctrl.exclusiveness = Scopes.toLocalRepresentation($ctrl.resource.data.exclusiveness);
				$ctrl.order = OrderSpecifications.toLocalRepresentation($ctrl.resource.data.orderSpecifications);
				loadTaskTypes();
				computePitches();
			}
		}
		
		function setPitchError(pitchError) {
			$ctrl.pitchError = pitchError;
		}
		
		function loadTaskTypes() {
			var resourcePromise = $ctrl.resource.data.type.phase
				? Phases.existingResource($ctrl.resource.model, $ctrl.resource.data.type.phase)
				: $q.when($ctrl.resource.model);
			return resourcePromise.then(loadTaskTypesFrom);
		}
		
		function loadTaskTypesFrom(resource) {
			// Reset old list of task types first so they cannot be selected.
			resource.getTaskTypes({
				projection: TaskTypes.Resource.prototype.defaultProjection
			})
			.then(Pages.collectRemaining)
			.then(function (taskTypes) {
				$ctrl.taskTypes = taskTypes;
			}, errorHandler.handle);			
		}
		
		function toggleCollapsed(fieldset) {
			$ctrl.collapsed[fieldset] = !$ctrl.collapsed[fieldset];
		}
		
		function editTaskDefinition() {
			var type = $ctrl.resource.data.type;
			$uibModal.open({
				component: 'preciseCreateTaskType',
				resolve: {
					resource: function () {
						return TaskTypes.existingResource($ctrl.resource.model, type)
							.then(function (tt) {
								// Nested entities do not include their own singular link rel,
								// which is the one we need since it has the 'projection' parameter.
								// --> Reload to fix that
								return tt.reload(); 
							})
							.then(function (tt) {
								return TaskTypes.existingResource($ctrl.resource.model, tt);
							});
					},
					phases: function () {
						return type.phase ? null : $ctrl.resource.model.getPhases({
							projection: TaskTypes.Resource.prototype.defaultProjection
						})
						.then(Pages.collectRemaining);
					},
					crafts: function () {
						return $ctrl.resource.model.getCrafts();
					}
				}
			}).result.then(function (result) {
				$ctrl.resource.data.type = result;
				$ctrl.taskDefinitionChanged({ $result: result });
				return loadTaskTypes();
			})['catch'](errorHandler.handle);
		}
		
		function computePitches() {
			$ctrl.resource.computePitches()
				.then(_.constant(null), PreciseApi.getErrorText)
				.then(setPitchError);
		}
		
		function updateExlusivenessType() {
			Scopes.updateType($ctrl.exclusiveness, getTaskAttributes($ctrl.resource.data));
		}
		
		function updateExclusivenessAttributes() {
			Scopes.updateAttributes($ctrl.exclusiveness, getTaskAttributes($ctrl.resource.data));
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
			var attr = _.find(getTaskAttributes($ctrl.resource.data), canSelectForOrderSpec);
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
			return _.size($ctrl.order.specs) < _.size(getTaskAttributes($ctrl.resource.data));
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
			}, errorHandler.handle);
		}
		
		function addPattern() {
			return $ctrl.resource.globalPattern().then(function (checkedPattern) {
				var num = $ctrl.resource.data.locationPatterns.length;
				$ctrl.resource.data.locationPatterns.push(checkedPattern);
				$timeout(function () {
					$anchorScroll('location-' + num);
				});
			}, errorHandler.handle);
		}
		
		function removePattern(index) {
			$ctrl.resource.data.locationPatterns.splice(index, 1);
		}
		
		function sendTask() {
			var attributes = getTaskAttributes($ctrl.resource.data),
				exclusiveness = attributes && Scopes.fromLocalRepresentation($ctrl.exclusiveness, attributes),
				orderSpecifications = attributes && OrderSpecifications.fromLocalRepresentation($ctrl.order, attributes);
			$ctrl.resource.data.exclusiveness = exclusiveness;
			$ctrl.resource.data.orderSpecifications = orderSpecifications;
			return $ctrl.resource.send('expandedTask')
				.then(function (result) {
					$ctrl.done({ $result: result });
				}, errorHandler.handle);
		}

		function isDisabledPatternEntry(patternEntry) {
			return _.get(patternEntry, ['allowedValues', 'length'], 0) <= 1;
		}
		
		function isDisabledOrderType(orderType, attribute) {
			return !OrderSpecifications.isAssignableTo(orderType, attribute);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/taskProperties.html',
		controller: TaskPropertiesController,
		controllerAs: '$ctrl',
		bindings: {
			resource: '<',
			done: '&',
			cancelled: '&',
			taskDefinitionChanged: '&'
		}
	};
	
});