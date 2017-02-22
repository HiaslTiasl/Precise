/**
 * Angular service for dealing with phase resources.
 * @module "api/Phases.service"
 */
define([
	'lib/lodash',
	'api/hal',
	'util/util'
], function (
	_,
	HAL,
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
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, phase) {
			return resource(model, phase, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model, phase) {
			return resource(model, phase, true);
		}
		
		/** Returns a promise of a model resource that already exists on the server. */
		function resource(model, phase, existing) {
			return $q.when(new PhaseResource(model, phase, existing));
		}
		
		/**
		 * Represents a phase resource.
		 * @constructor
		 * @extends module:"api/Resource.service"#Base
		 */
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
		
			getActivities: function (params) {
				var self = this;
				return PreciseApi.fromBase()
					.traverse(function (builder) {
						return builder
							.follow('activities', 'search', 'findByPhase')
							.withTemplateParameters(_.assign({
								phase: HAL.resolve(HAL.hrefTo(self.data)),
							}, params))
							.get();
					}).then(Pages.wrapper('activities'));
			}
		
		});

	}
	
	return PhasesService;
	
});