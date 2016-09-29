define([
	'lib/lodash',
	'api/colors'
], function (
	_,
	colors
) {
	'use strict';
	
	SingleModelBuildingController.$inject = ['Phases'];
	
	function SingleModelBuildingController(Phases) {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		$ctrl.sendPhase = sendPhase;
		
		$ctrl.$onChanges = $onChanges;
		
		$ctrl.color = {
			value: null,
			options: {
				format: 'rgb',
				alpha: false,
				swatch: true,
				swatchOnly: true,
				swatchBootstrap: false
			},
			eventApi: {
				onChange: colorChanged
			}
		};
		
		function $onChanges(changes) {
			if (changes.phases) {
				//$ctrl.color.options.disabled = !$ctrl.model.data.configInfo.editable;
				phasesChanged();
			}
		}
		
		function phasesChanged() {
			if ($ctrl.phases && $ctrl.phases.length)
				$ctrl.phase = $ctrl.phases[0];
			else
				$ctrl.phase = null;
			phaseChanged();
		}
		
		function phaseChanged() {
			$ctrl.color.value = $ctrl.phase ? colors.toRgb($ctrl.phase.color) : null;
		}
		
		function colorChanged() {
			$ctrl.phase.color = colors.fromCSS($ctrl.color.value);
			sendPhase();
		}
		
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
		templateUrl: 'js/singleModel/singleModel-building.html',
		controller: SingleModelBuildingController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			phases: '<',
			reload: '&'
		}
	};
	
});
