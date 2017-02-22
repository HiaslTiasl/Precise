/**
 * Angular component for the activities view of the configuration part.
 * @module "singleModel/SingleModel-activities.component"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	ActivitiesController.$inject = ['$q', '$uibModal', 'errorHandler', 'PreciseApi', 'Pages', 'Activities'];
	
	/**
	 * Controller constructor
	 * @constructor
	 */
	function ActivitiesController($q, $uibModal, errorHandler, PreciseApi, Pages, Activities) {
		var $ctrl = this;
		
		$ctrl.createActivity = createActivity;
		$ctrl.editActivity = editActivity;
		$ctrl.deleteActivity = deleteActivity;
		
		$ctrl.$onChanges = $onChanges;
		
		/** Specialized error handler for conflicts on deletion. */
		var deleteErrorHandler = errorHandler.wrapIf(PreciseApi.isHttpConflict, {
			title: 'Cannot delete activity',
			message: 'The tasks referencing this activity cannot be deleted automatically'
		});
		
		function $onChanges(changes) {
			if (changes.model) {
				loadActivities();
			}
		}
		
		/** Loads the list of activities from the model. */
		function loadActivities() {
			$ctrl.model.getActivities({ projection: 'expandedActivity' })
				.then(setActivities, errorHandler.handle);
		}
		
		/** Sets the given list of activities. */
		function setActivities(activities) {
			$ctrl.activities = activities;
		}
		
		/** Returns a promise of the crafts of the model, and caches the result. */
		var getCrafts = _.once(function () {
			return $ctrl.model.getCrafts();
		});
		
		/** Opens the given activity resource in a modal dialog. */
		function openModal(resource) {
			$uibModal.open({
				component: 'activitiesDialog',
				resolve: {
					resource: resource,
					phases: _.constant($ctrl.phases),
					crafts: getCrafts
				}
			}).result.then(loadActivities);
		}
		
		/** Opens a modal dialog for creating a activity. */
		function createActivity() {
			openModal(function () {
				return Activities.newResource($ctrl.model);
			});
		}
		
		/** Opens a modal dialog for editing the given activity. */
		function editActivity(activity) {
			openModal(function () {
				return Activities.existingResource($ctrl.model, _.clone(activity));
			});
		}
		
		/**
		 * Deletes the given activity.
		 * If it has any task boxes, the user is prompted to confirm the deletion.
		 */
		function deleteActivity(activity) {
			var permission = !activity.taskCount ? $q.when() :  PreciseApi.asyncConfirm([
				'There are tasks referencing this activity',
				'',
				'If you delete the activity, you will lose these tasks as well.',
				'Delete anyway?'
			].join('\n'));
			
			return permission.then(function () {
				return Activities
					.existingResource($ctrl.model, activity)
					.then(function (resource) {
						return resource.delete();
					})
					.then(loadActivities, deleteErrorHandler.handle);
				// N.B. while it is possible to handle the success case outside in a chained
				// .then call, the same is not possible for handling the failure case, because
				// it would also be called if the user rejects the deletion.
			});
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/Activities.html',
		controller: ActivitiesController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
