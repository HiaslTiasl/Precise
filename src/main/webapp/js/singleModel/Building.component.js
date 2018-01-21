/**
 * Angular component for the building (or phases) view of the configuration
 * @module "singleModule/SingleModule-building.component"
 */
define([
	'lib/lodash',
	'api/colors'
], function (
	_,
	colors
) {
	'use strict';
	
	BuildingController.$inject = ['Phases'];
	
	/**
	 * Controller constructor
	 * @constructor
	 */
	function BuildingController(Phases) {
		
		var $ctrl = this;
		
		$ctrl.hoursPerDayChanged = hoursPerDayChanged;
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.sendPhase = sendPhase;
		
		$ctrl.$onChanges = $onChanges;
		
		// Options for angular-color-picker
		$ctrl.color = {
			value: null,
			options: {
				format: 'rgb',				// Use rgb format, e.g. 'rgb(255,255,255)', 
				alpha: false,				// Do not use alpha channel
				swatch: true,				// Show swatch
				swatchOnly: true,			// Only show swatch
				swatchBootstrap: false		// Do not use bootstrap styles
			},
			eventApi: {
				onClose: colorChanged		// Listen to close event
			}
		};
		
		function $onChanges(changes) {
			if (changes.phases) {
				phasesChanged();
				phaseChanged();
			}
		}
		
		/** The phases changed, so update the selection of the current phase. */
		function phasesChanged() {
			if (!_.size($ctrl.phases))		// Model has no phases -> cannot select any
				$ctrl.phase = null;
			else {
				// Try to match a previous phase by name
				if ($ctrl.phase)
					$ctrl.phase = _.find($ctrl.phases, { name: $ctrl.phase.name });
				// Select the first phase if no previous phase or no match
				if (!$ctrl.phase)
					$ctrl.phase = $ctrl.phases[0];
			}
		}
		
		/** The phase selection changed, so update the color of the color picker. */
		function phaseChanged() {
			$ctrl.color.value = $ctrl.phase ? colors.toRgb($ctrl.phase.color) : null;
		}
		
		/** The color changed, so send the phase to the server to apply the change. */
		function colorChanged() {
			$ctrl.phase.color = colors.fromCSS($ctrl.color.value);
			sendPhase();
		}
		
		/** The working hours per day changed, so send the model to the server to apply the change. */
		function hoursPerDayChanged() {
			$ctrl.model.send().then($ctrl.reload);
		}
		
		/** Send the phase to the server to apply any changes. */
		function sendPhase() {
			Phases
				.existingResource($ctrl.model, $ctrl.phase)
				.then(function (resource) {
					return resource.update({ projection: 'expandedPhase' });
				})
				.then($ctrl.reload);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/Building.html',
		controller: BuildingController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
