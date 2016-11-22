define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	TaskTypesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	function TaskTypesService($q, PreciseApi, Resources, Pages) {
		
		var TaskTypes = this;
		
		TaskTypes.newResource = newResource;
		TaskTypes.existingResource = existingResource;
		TaskTypes.resource = resource;
		TaskTypes.Resource = TaskTypeResource;
		
		var dontSendDirectly = ['craft', 'phase', 'model', '_links'];
		
		function newResource(model, taskType) {
			return resource(model, taskType, false);
		}
		
		function existingResource(model, taskType) {
			return resource(model, taskType, true);
		}
		
		function resource(model, taskType, existing) {
			return $q.when(new TaskTypeResource(model, taskType, existing));
		}
		
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
			
			getRequestData: function () {
				var processed = _.omit(this.data, dontSendDirectly);
				if (this.data.craft)
					processed.craft = PreciseApi.hrefTo(this.data.craft);
				if (this.data.phase)
					processed.phase = PreciseApi.hrefTo(this.data.phase);
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				return processed;
			}
		
		});

	}
	
	return TaskTypesService;
	
});