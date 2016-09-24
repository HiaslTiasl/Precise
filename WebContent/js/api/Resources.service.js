define([
	'lib/lodash',
	'util/util'
], function (
	_,
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
			this.data = data;
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
				var projection = requestedProjection !== undefined ? requestedProjection : self.defaultProjection;
				return projection && {
					projection: projection
				};
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
				var self = this;
				return PreciseApi.from(PreciseApi.hrefTo(self.data, self.rels.singular))
					.traverse(function (builder) {
						return builder
							.withTemplateParameters(self.getTemplateParams(projection))
							.get();
					});
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
			}
			
		});

	}
	
	return ResourcesService;
	
});