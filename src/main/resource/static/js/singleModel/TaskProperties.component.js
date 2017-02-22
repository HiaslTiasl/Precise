/**
 * Angular component for task properties.
 * @module "singleModel/taskProperties.component"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskPropertiesController.$inject = ['$q', '$uibModal', '$anchorScroll', '$timeout', 'errorHandler', 'PreciseApi', 'Tasks', 'Activities', 'Scopes', 'OrderSpecifications', 'Phases', 'Pages'];
	
	function TaskPropertiesController($q, $uibModal, $anchorScroll, $timeout, errorHandler, PreciseApi, Tasks, Activities, Scopes, OrderSpecifications, Phases, Pages) {
		var $ctrl = this;
		
		$ctrl.editActivity = editActivity;
		$ctrl.computePitches = computePitches;
		$ctrl.updateExlusivenessType = updateExlusivenessType;
		$ctrl.updateExclusivenessAttributes = updateExclusivenessAttributes;
		$ctrl.attrFilterForOrderSpec = attrFilterForOrderSpec;
		$ctrl.changedOrderEntryAttribute = changedOrderEntryAttribute
		$ctrl.addDefaultOrderEntry = addDefaultOrderEntry;
		$ctrl.canAddOrderEntry = canAddOrderEntry;
		$ctrl.isDisabledOrderType = isDisabledOrderType;
		$ctrl.addPattern = addPattern;
		$ctrl.removePattern = removePattern;
		$ctrl.updatePattern = updatePattern;
		$ctrl.isDisabledPatternEntry = isDisabledPatternEntry;
		$ctrl.sendTask = sendTask;
		$ctrl.toggleCollapsed = toggleCollapsed;
		
		/** Scope types in display order. */
		$ctrl.scopeTypes = [
			Scopes.Types.UNIT,
			Scopes.Types.GLOBAL,
			Scopes.Types.ATTRIBUTES
		];
		
		/** Order types in display order. */
		$ctrl.orderTypes = [
			OrderSpecifications.Types.NONE,
			OrderSpecifications.Types.PARALLEL,
			OrderSpecifications.Types.ASCENDING,
			OrderSpecifications.Types.DESCENDING
		];
		
		/** Indicates which of the fieldsets are collapsed. */
		$ctrl.collapsed = {
			pitches: true,
			exclusiveness: true,
			order: true,
			locations: true
		};

		$ctrl.$onChanges = $onChanges;
		
		/** Returns all attributes for locations of the given tasks. */
		var getTaskAttributes = _.property(['activity', 'phase', 'attributes']);
		
		function $onChanges() {
			if ($ctrl.resource) {
				// Use local representations of scope and ordering
				$ctrl.exclusiveness = Scopes.toLocalRepresentation($ctrl.resource.data.exclusiveness);
				$ctrl.order = OrderSpecifications.toLocalRepresentation($ctrl.resource.data.orderSpecifications);
				// Init available activities and validate pitch parameters
				loadActivities();
				computePitches();
			}
		}
		
		/** Show the given error regarding pitch parameters. */
		function setPitchError(pitchError) {
			$ctrl.pitchError = pitchError;
		}
		
		/**
		 * Loads the list of activities, either from the phase, if available,
		 * or from the model otherwise.
		 */
		function loadActivities() {
			var resourcePromise = $ctrl.resource.data.activity.phase
				? Phases.existingResource($ctrl.resource.model, $ctrl.resource.data.activity.phase)
				: $q.when($ctrl.resource.model);
			return resourcePromise.then(loadActivitiesFrom);
		}
		
		/**
		 * Loads the list of activities associated to the given resource,
		 * which can be either a model or a phase.
		 */
		function loadActivitiesFrom(resource) {
			// Reset old list of activities first so they cannot be selected.
			resource.getActivities({
				projection: Activities.Resource.prototype.defaultProjection
			})
			.then(Pages.collectRemaining)
			.then(function (activities) {
				$ctrl.activities = activities;
			}, errorHandler.handle);			
		}
		
		/** Toggle collapsing of the given fieldset. */
		function toggleCollapsed(fieldset) {
			$ctrl.collapsed[fieldset] = !$ctrl.collapsed[fieldset];
		}
		
		/** Opens a dialog for editing the selected activity. */
		function editActivity() {
			var activity = $ctrl.resource.data.activity;
			$uibModal.open({
				component: 'ActivitiesDialog',
				resolve: {
					resource: function () {
						return Activities.existingResource($ctrl.resource.model, activity)
							.then(function (a) {
								// Reload to ensure that we are dealing with a first-class resource
								return a.reload(); 
							})
							.then(function (a) {
								return Activities.existingResource($ctrl.resource.model, a);
							});
					},
					phases: function () {
						// Fix phase to the current one if available
						return activity.phase ? null : $ctrl.resource.model.getPhases({
							projection: Activities.Resource.prototype.defaultProjection
						})
						.then(Pages.collectRemaining);
					},
					crafts: function () {
						return $ctrl.resource.model.getCrafts();
					}
				}
			}).result.then(function (result) {
				$ctrl.resource.data.activity = result;
				// Call outer change handler
				$ctrl.activityChanged({ $result: result });
				return loadActivities();
			});
		}
		
		/** 
		 * Ask the server to compute missing pitch parameters and resulting
		 * man-hours, or to check whether the given parameters are consistent.
		 */
		function computePitches() {
			$ctrl.resource.computePitches()
				.then(_.constant(null), PreciseApi.getErrorText)
				.then(setPitchError);
		}
		
		/** Updates type of exclusiveness scope based on scope attributes and available attributes. */
		function updateExlusivenessType() {
			Scopes.updateType($ctrl.exclusiveness, getTaskAttributes($ctrl.resource.data));
		}
		
		/** Updates attributes of exclusiveness scope based on scope type and available attributes. */
		function updateExclusivenessAttributes() {
			Scopes.updateAttributes($ctrl.exclusiveness, getTaskAttributes($ctrl.resource.data));
		}
		
		/**
		 * Returns a function that indicates whether a given attribute can be selected
		 * as the new attribute for the given order specification.
		 * Useful as an argument to the Angular filter "filter"
		 * @example
		 * 	... attrs | filter:$ctrl.attrFilterForOrderSpec(os)
		 */
		function attrFilterForOrderSpec(os) {
			return function (a) {
				return a.name === os.attribute.name		// The selected option must be seletible
					|| (!$ctrl.order.hasAttribute(a) && !isDisabledOrderType(os, a));
			}
		}
		
		/**
		 * An attribute was selected for an order spec, so update the local representation to refresh
		 * the set of attributes in use.
		 */
		function changedOrderEntryAttribute() {
			$ctrl.order.check();
		}
		
		/** Adds a new entry to the order specification if possible. */
		function addDefaultOrderEntry() {
			$ctrl.order.addDefaultEntry(getTaskAttributes($ctrl.resource.data));
		}
		
		/** Indicates whether another entry can be added to the order specification. */
		function canAddOrderEntry() {
			return $ctrl.order.canAddEntry(getTaskAttributes($ctrl.resource.data));
		}
		
		/** Indicates whether the given order type is disabled for the given attribute. */
		function isDisabledOrderType(orderType, attribute) {
			return !OrderSpecifications.isAssignableTo(orderType, attribute);
		}
		
		/** Updates the given attribute to the given value in the given pattern. */
		function updatePattern(pattern, patternNum, attr, newValue) {
			return $ctrl.resource.updatePattern(pattern, attr, newValue).then(function (checkedPattern) {
				// Copy all properties from the checked pattern to the current one.
				// N.B: In principle we could also just replace the whole pattern at once,
				// but SmartTable does not notice that and the view is not updated.
				// See https://github.com/lorenzofox3/Smart-Table/issues/205
				// Also note that we must consider the whole pattern returned, not
				// only the updated attribute, since, in principle, the server can update
				// all attributes (in practice only the changed and the following ones).
				_.assign($ctrl.resource.data.locationPatterns[patternNum], checkedPattern);
			}, errorHandler.handle);
		}
		
		/**
		 * Add another location pattern to the table. The new pattern is initialized as a global
		 * pattern, which can be refined by the user afterwards.
		 */
		function addPattern() {
			return $ctrl.resource.globalPattern().then(function (checkedPattern) {
				var num = $ctrl.resource.data.locationPatterns.length;
				$ctrl.resource.data.locationPatterns.push(checkedPattern);
				// We add a location to the bottom of a table that is itself at the bottom
				// of the properties scroll pane, so it is likely not visible without scrolling,
				// so we do the scrolling for the user.
				$timeout(function () {
					$anchorScroll('location-' + num);
				});
			}, errorHandler.handle);
		}
		
		/** Removes the location pattern at the given index.  */
		function removePattern(index) {
			$ctrl.resource.data.locationPatterns.splice(index, 1);
		}

		/** Indicates whether editing the given pattern entry should be disabled. */
		function isDisabledPatternEntry(patternEntry) {
			return _.get(patternEntry, ['allowedValues', 'length'], 0) <= 1;
		}
		
		/** Sends the task to the server to apply the changes. */
		function sendTask() {
			var attributes = getTaskAttributes($ctrl.resource.data),
				exclusiveness = attributes && Scopes.fromLocalRepresentation($ctrl.exclusiveness, attributes),
				orderSpecifications = attributes && OrderSpecifications.fromLocalRepresentation($ctrl.order, attributes);
			$ctrl.resource.data.exclusiveness = exclusiveness;
			$ctrl.resource.data.orderSpecifications = orderSpecifications;
			return $ctrl.resource.send('expandedTask')
				.then(function (result) {
					// Call outer change handler 
					$ctrl.done({ $result: result });
				}, errorHandler.handle);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/TaskProperties.html',
		controller: TaskPropertiesController,
		controllerAs: '$ctrl',
		bindings: {
			resource: '<',
			done: '&',
			cancelled: '&',
			activityChanged: '&'
		}
	};
	
});