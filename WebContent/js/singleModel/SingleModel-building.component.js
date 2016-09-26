define([
	'lib/lodash',
	'api/colors'
], function (
	_,
	colors
) {
	'use strict';
	
	function SingleModelBuildingController() {
		
		var $ctrl = this;
		
		$ctrl.phaseChanged = phaseChanged;
		
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
			if (changes.model) {
				$ctrl.color.options.disabled = !$ctrl.model.data.configInfo.editable;
			}
			if (changes.config) {
				if ($ctrl.config && $ctrl.config.phases && $ctrl.config.phases.length)
					$ctrl.phase = $ctrl.config.phases[0];
				else
					$ctrl.phase = null;
				phaseChanged();
			}
		}
		
		function phaseChanged() {
			$ctrl.color.value = $ctrl.phase ? colors.toRgb($ctrl.phase.color) : null;
		}
		
		function colorChanged() {
			$ctrl.phase.color = colors.fromCSS($ctrl.color.value);
		}
		
	}
	
	return {
		templateUrl: 'js/singleModel/singleModel-building.html',
		controller: SingleModelBuildingController,
		controllerAs: '$ctrl',
		bindings: {
			model: '<',
			config: '<'
		}
	};
	
});
