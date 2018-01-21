/**
 * Angular service for dealing with craft resources.
 * @module "api/Crafts.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	CraftsService.$inject = ['$q', 'PreciseApi', 'Resources', 'Pages'];
	
	/** @constructor */
	function CraftsService($q, PreciseApi, Resources, Pages) {
		
		var Crafts = this;
		
		Crafts.newResource = newResource;
		Crafts.existingResource = existingResource;
		Crafts.resource = resource;
		Crafts.Resource = CraftResource;
		
		var dontSendDirectly = ['_links'];		// Properties that should not be sent as they are
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, craft) {
			return resource(model, craft, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model, craft) {
			return resource(model, craft, true);
		}
		
		/** Returns a promise of a new craft resource. */
		function resource(model, craft, existing) {
			// N.B. Actually we already have all the data, so technically
			// there is no need to return a promise, but we do anyway
			// in the favor of a uniform interface across resource services.
			return $q.when(new CraftResource(model, craft, existing));
		}
		
		/**
		 * A resource representing a craft
		 * @constructor
		 * @extends module:"api/Resource.service"#Base
		 */
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
				// omit special fields
				var processed = _.omit(this.data, dontSendDirectly);
				// set model link for new resources
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				return processed;
			}
		
		});

	}
	
	return CraftsService;
	
});