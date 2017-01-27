/**
 * Angular service for dealing with resources.
 * @module "api/Resources.service"
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
	
	ResourcesService.$inject = ['PreciseApi'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function ResourcesService(PreciseApi) {
		
		var Resources = this;
		
		Resources.guessExisting = guessExisting;
		Resources.Base = BaseResource;
		
		/** Determines whether the resource with the given data already exists based on links. */
		function guessExisting(data) {
			return !!PreciseApi.linkTo(data);
		}
		
		/**
		 * Represents a resource.
		 * @constructor
		 */
		function BaseResource(data, exists) {
			this.data = data || {};
			this.exists = exists !== undefined ? exists : guessExisting(data);
		}
		
		util.defineClass({
			
			constructor: BaseResource,
			
			rels: {
				singular: null,		// Relation for a single resource (i.e. link to self with template params) 
				plural: null		// Relation to multiple resources
			},

			/** 
			 * Returns the data to be sent with a request.
			 * May be overridden by subclasses, possibly differentiating between
			 * existing and non existing resources.
			 */
			getRequestData: function () {
				return this.data;
			},
			
			/**
			 * Helper method that returns template parameters that use
			 * the given projection is specified, or the default one otherwise.
			 */
			getTemplateParams: function (requestedProjection) {
				var projection = requestedProjection !== undefined ? requestedProjection : this.defaultProjection;
				return projection && {
					projection: projection
				};
			},
			
			/**
			 * Returns a URL to the given relation, using the given template params.
			 * If params contains a projection, the projection is appended as a query parameter
			 * even if this is not specified in the template.
			 * This is a workaround for a bug in Spring Data REST that it does not add templates
			 * to associations.
			 */
			getURL: function (rel, params) {
				var url = HAL.resolve(HAL.hrefTo(this.data, rel), params);
				if (params && params.projection) {
					var query = 'projection=' + params.projection;	// The projection as a query param
					if (!_.includes(url, '?'))						// The URL has no query part
						url = url + '?' + query;					// -> add the projection as the query part
					else if (!_.includes(url, query))				// The URL has a query part but it does not include the projection
						url = url + '&' + query;					// -> add the projection as another key-value pair
				}
				return url;
			},
			
			/** Create this (nonexisting) resource on the server. */
			create: function (projection) {
				var self = this;
				return PreciseApi.fromBase()
					.traverse(function (builder) {
						return builder
							.follow(self.rels.plural)
							.withTemplateParameters(self.getTemplateParams(projection))
							.post(self.getRequestData());
					});
			},
			
			/** Reload the resource from the server. */
			reload: function (projection) {
				return PreciseApi.from(this.getURL('self', this.getTemplateParams(projection)))
					.followAndGet();
			},
			
			/** Update this (existing) resource on the server. */
			update: function (projection) {
				var self = this;
				return PreciseApi.from(PreciseApi.hrefTo(self.data, self.rels.singular))
					.traverse(function (builder) {
						return builder
							.withTemplateParameters(self.getTemplateParams(projection))
							.patch(self.getRequestData());
					});
			},
			
			/**
			 * Save this resource on the server.
			 * If it already exists, it is updated, otherwise it is created.
			 */
			send: function (projection) {
				return this.exists ? this.update(projection) : this.create(projection);
			},
			
			/** Delete this resource on the server. */
			delete: function () {
				return PreciseApi.continueFrom(this.data)
					.traverse(function (builder) {
						return builder.del();
					});
			},
			
			/** Get the resource of the given relation and template parameters. */
			get: function (rel, params) {
				return PreciseApi.from(this.getURL(rel, params)).followAndGet();
			},
			
			/** Get the resource of the given relation and template parameters as a paged resource. */
			getPage: function (rel, params) {
				return this.get(rel, params).then(Pages.wrapper(rel));
			},
			
			/** Get the resource of the given relation and template parameters as a list of embedded resources. */
			getList: function (rel, params) {
				return this.get(rel, params).then(function (res) {
					return PreciseApi.embeddedArray(res, rel);
				});
			}
			
		});

	}
	
	return ResourcesService;
	
});