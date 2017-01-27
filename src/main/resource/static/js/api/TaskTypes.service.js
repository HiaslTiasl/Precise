/**
 * Angular service for task type resources.
 * @module "api/TaskTypes.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskTypesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function TaskTypesService($q, PreciseApi, Resources, Pages) {
		
		var TaskTypes = this;
		
		TaskTypes.newResource = newResource;
		TaskTypes.existingResource = existingResource;
		TaskTypes.resource = resource;
		TaskTypes.Resource = TaskTypeResource;
		
		var dontSendDirectly = ['craft', 'phase', 'model', '_links'];
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, taskType) {
			return resource(model, taskType, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model, taskType) {
			return resource(model, taskType, true);
		}
		
		/** Returns a promise of a task type resource. */
		function resource(model, taskType, existing) {
			return $q.when(new TaskTypeResource(model, taskType, existing));
		}
		
		/**
		 * Represents a task type resource
		 * @constructor
		 * @extends module:"api/Resources.service"#Base
		 */
		function TaskTypeResource(model, data, existing) {
			Resources.Base.call(this, data, existing);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: TaskTypeResource,

			rels: {
				singular: 'taskType',
				plural: 'taskTypes'
			},
			
			defaultProjection: 'expandedTaskType',
			
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
	
	return TaskTypesService;
	
});