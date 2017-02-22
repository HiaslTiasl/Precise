/**
 * Angular service for activity resources.
 * @module "api/Activities.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	ActivitiesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function ActivitiesService($q, PreciseApi, Resources, Pages) {
		
		var Activities = this;
		
		Activities.newResource = newResource;
		Activities.existingResource = existingResource;
		Activities.resource = resource;
		Activities.Resource = ActivityResource;
		
		var dontSendDirectly = ['craft', 'phase', 'model', '_links'];
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, activity) {
			return resource(model, activity, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model, activity) {
			return resource(model, activity, true);
		}
		
		/** Returns a promise of an activity resource. */
		function resource(model, activity, existing) {
			return $q.when(new ActivityResource(model, activity, existing));
		}
		
		/**
		 * Represents an activity resource
		 * @constructor
		 * @extends module:"api/Resources.service"#Base
		 */
		function ActivityResource(model, data, existing) {
			Resources.Base.call(this, data, existing);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: ActivityResource,

			rels: {
				singular: 'activity',
				plural: 'activities'
			},
			
			defaultProjection: 'expandedActivity',
			
			getTasks: function (params) {
				return this.getList('tasks', params);
			},
			
			getRequestData: function () {
				// omit special fields
				var processed = _.omit(this.data, dontSendDirectly);
				// use links for associations to existing resources
				if (this.data.craft)
					processed.craft = PreciseApi.hrefTo(this.data.craft);
				if (this.data.phase)
					processed.phase = PreciseApi.hrefTo(this.data.phase);
				// set model link for new resources
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				return processed;
			}
		
		});

	}
	
	return ActivitiesService;
	
});