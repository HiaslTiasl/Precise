/**
 * Angular component for viewing and setting task properties in a dialog.
 * @module "singleModel/singleModel-diagram.create-task-dialog.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	DiagramTaskDialogController.$inject = ['$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'Tasks', 'Activities', 'Phases'];
	
	/**
	 * Controller constructor.
	 * @constructor
	 */
	function DiagramTaskDialogController($uibModal, errorHandler, PreciseApi, Pages, Tasks, Activities, Phases) {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.activityChanged = activityChanged;
		$ctrl.createActivity = createActivity;
		$ctrl.computePitches = computePitches;
		$ctrl.send = send;
		$ctrl.cancel = cancel;
		
		$ctrl.$onInit = $onInit;
		
		function $onInit() {
			$ctrl.resource = $ctrl.resolve.resource;
			$ctrl.phases = $ctrl.resolve.phases;
			resetPhase();
		}
		
		/** Show the given error regarding pitch parameters. */
		function setPitchError(pitchError) {
			$ctrl.pitchError = pitchError;
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
		
		/** Restrict the available activities to the given list. */
		function setActivities(activities) {
			$ctrl.activities = activities;
		}
		
		/** The current phase changed, so update the available activities. */
		function phaseChanged() {
			if ($ctrl.phase)
				setPhase();
			else
				resetPhase();
		}
		
		/** The phase was reset, so let the user select all activities of the model. */
		function resetPhase() {
			loadActivitiesFrom($ctrl.resource.model);
		}
		
		/** A phase was set, so restrict the available activities to those of that phase. */
		function setPhase() {
			Phases.existingResource($ctrl.resource.model, $ctrl.phase)
				.then(loadActivitiesFrom);			
		}
		
		/**
		 * Loads the list of activities associated to the given resource,
		 * which can be either a model or a phase.
		 */
		function loadActivitiesFrom(resource) {
			// Reset old list of activities first so they cannot be selected.
			setActivities(null);
			resource.getActivities({
				projection: Activities.Resource.prototype.defaultProjection
			})
			.then(Pages.collectRemaining)
			.then(setActivities);			
		}

		/** The activity changed, so update the phase accordingly. */
		function activityChanged() {
			if ($ctrl.phase != $ctrl.resource.data.activity.phase) 
				$ctrl.phase = $ctrl.resource.data.activity.phase;
		}
		
		/** Returns a promise of the crafts of the model, and caches the result. */
		var getCrafts = _.once(function () {
			return $ctrl.resource.model.getCrafts();
		});
		
		/** Opens a dialog for creating an activity that is to be used in this task. */
		function createActivity() {
			$uibModal.open({
				component: 'ActivityDialog',
				resolve: {
					resource: function () {
						return Activities.newResource($ctrl.resource.model, {
							phase: $ctrl.phase
						});
					},
					phases: _.constant($ctrl.phases),	// Restrict the available phases
					crafts: getCrafts
				}
			}).result.then(function (result) {
				$ctrl.activities.push(result);
				$ctrl.resource.data.activity = result;
				activityChanged();
			}, errorHandler.handle);
		}
		
		/** Send the task resource to apply the changes on the server and close the dialog. */
		function send() {
			$ctrl.resource.send()
				.then($ctrl.modalInstance.close);
		}
		
		/** Cancel editing and dismiss the dialog. */
		function cancel() {
			$ctrl.modalInstance.dismiss('cancel');
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/DiagramTaskDialog.html',
		controller: DiagramTaskDialogController,
		controllerAs: '$ctrl',
		bindings: {
			resolve: '<',
			modalInstance: '<'
		}
	}
	
});
