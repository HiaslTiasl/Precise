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
	
	function ResourcesService(PreciseApi) {
		
		var Resources = this;
		
		Resources.guessExisting = guessExisting;
		Resources.Base = BaseResource;
		
		function guessExisting(data) {
			return !!PreciseApi.linkTo(data);
		}
		
		function BaseResource(data, exists) {
			this.data = data || {};
			this.exists = exists !== undefined ? exists : guessExisting(data);
		}
		
		util.defineClass({
			
			rels: {
				singular: null,
				plural: null
			},
			
			constructor: BaseResource,
			
			getRequestData: function () {
				return this.data;
			},
			
			getTemplateParams: function (requestedProjection) {
				var projection = requestedProjection !== undefined ? requestedProjection : this.defaultProjection;
				return projection && {
					projection: projection
				};
			},
			
			getURL: function (rel, params) {
				var url = PreciseApi.hrefTo(this.data, rel);
				if (params) {
					url = HAL.resolve(url);
					if (params.projection) {
						var query = 'projection=' + params.projection;
						if (!_.includes(url, '?'))
							url = url + '?' + query;
						else if (!_.includes(url, query))
							url = url + '&' + query;
					}
				}
				return url;
			},
			
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
			
			reload: function (projection) {
				return PreciseApi.from(this.getURL('self', this.getTemplateParams(projection)))
					.followAndGet();
			},
			
			update: function (projection) {
				var self = this;
				return PreciseApi.from(PreciseApi.hrefTo(self.data, self.rels.singular))
					.traverse(function (builder) {
						return builder
							.withTemplateParameters(self.getTemplateParams(projection))
							.patch(self.getRequestData());
					});
			},
			
			send: function (projection) {
				return this.exists ? this.update(projection) : this.create(projection);
			},
			
			delete: function () {
				return PreciseApi.continueFrom(this.data)
					.traverse(function (builder) {
						return builder.del();
					});
			},
			
			get: function (rel, params) {
				return PreciseApi.from(this.getURL(rel, params)).followAndGet();
			},
			
			getPage: function (rel, params) {
				return this.get(rel, params).then(Pages.wrapper(rel));
			},
			
			getList: function (rel, params) {
				return this.get(rel, params).then(function (res) {
					return PreciseApi.embeddedArray(res, rel);
				});
			}
			
		});

	}
	
	return ResourcesService;
	
});