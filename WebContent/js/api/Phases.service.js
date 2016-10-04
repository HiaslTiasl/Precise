define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	PhasesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	function PhasesService($q, PreciseApi, Resources, Pages) {
		
		var Phases = this;
		
		Phases.newResource = newResource;
		Phases.existingResource = existingResource;
		Phases.resource = resource;
		Phases.Resource = PhaseResource;
		
		function newResource(model, phase) {
			return resource(model, phase, false);
		}
		
		function existingResource(model, phase) {
			return resource(model, phase, true);
		}
		
		function resource(model, phase, existing) {
			return $q.when(new PhaseResource(model, phase, existing));
		}
		
		function PhaseResource(model, data, existing) {
			Resources.Base.call(this, data, existing);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: PhaseResource,

			rels: {
				singular: 'phase',
				plural: 'phases'
			},
			
			defaultProjection: 'phaseSummary',
		
			getTaskTypes: function () {
				var self = this;
				return PreciseApi.fromBase()
					.traverse(function (builder) {
						return builder
							.follow('taskTypes', 'search', 'findByPhase')
							.withTemplateParameters({
								phase: PreciseApi.hrefTo(self.data)
							})
							.get();
					}).then(Pages.wrapper('taskTypes'));
			}
		
		});

	}
	
	return PhasesService;
	
});