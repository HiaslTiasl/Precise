define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	CraftsService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	function CraftsService($q, PreciseApi, Resources, Pages) {
		
		var Crafts = this;
		
		Crafts.newResource = newResource;
		Crafts.existingResource = existingResource;
		Crafts.resource = resource;
		Crafts.Resource = CraftResource;
		
		var dontSendDirectly = ['_links'];
		
		function newResource(model, taskType) {
			return resource(model, taskType, false);
		}
		
		function existingResource(model, taskType) {
			return resource(model, taskType, true);
		}
		
		function resource(model, taskType, existing) {
			return $q.when(new CraftResource(model, taskType, existing));
		}
		
		function CraftResource(model, data, existing) {
			Resources.Base.call(this, data, existing);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: CraftResource,

			rels: {
				singular: 'craft',
				plural: 'crafts'
			},
			
			defaultProjection: null,
			
			getRequestData: function () {
				var processed = _.omit(this.data, dontSendDirectly);
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				return processed;
			}
		
		});

	}
	
	return CraftsService;
	
});