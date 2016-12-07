define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	ModelsService.$inject = ['PreciseApi', 'Resources', 'Pages'];
	
	function ModelsService(PreciseApi, Resources, Pages) {
		
		var Models = this;
		
		Models.findAll = findAll;
		Models.firstPage = firstPage;
		Models.findByName = findByName;
		Models.findByNameWithPartInfos = findByNameWithPartInfos;
		Models.newResource = newResource;
		Models.existingResource = existingResource;
		Models.resource = resource;
		Models.Resource = ModelResource;
		
		var REL_SINGULAR = 'model',
			REL_PLURAL = 'models';
		
		function firstPage(templateParams) {
			return PreciseApi.fromBase().traverse(function (builder) {
				return builder
					.follow(REL_PLURAL)
					.withTemplateParameters(templateParams)
					.get();
			}).then(Pages.wrapper(REL_PLURAL));
		}
		
		function findAll(templateParams) {
			return firstPage(templateParams)
				.then(function (page) {
					return page.collectRemaining(templateParams);
				});
		}
		
		function findByName(name, templateParams) {
			return PreciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow(REL_PLURAL, 'search', 'findByName')
						.withTemplateParameters(_.defaults({
							name: name
						}, templateParams))
						.get()
				}).then(Models.existingResource);
		}
		
		function findByNameWithPartInfos(name) {
			return findByName(name, {
				projection: 'modelSummary'
			});
		}
		
		function newResource(model) {
			return resource(model, false);
		}
		
		function existingResource(model) {
			return resource(model, true);
		}
		
		function resource(model, existing) {
			return new ModelResource(model, existing);
		}
		
		function ModelResource(data, existing) {
			Resources.Base.call(this, data, existing);
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: ModelResource,

			rels: {
				singular: REL_SINGULAR,
				plural: REL_PLURAL
			},
			
			defaultProjection: null,
		
			getTasks: function (params) {
				return this.getList('tasks', params);
			},
			
			getDependencies: function (params) {
				return this.getList('dependencies', params);
			},
			
			getPhases: function (params) {
				return this.getList('phases', params);
			},
			
			getCrafts: function (params) {
				return this.getList('crafts', params);
			},
			
			getTaskTypes: function (params) {
				return this.getList('taskTypes', params);
			},
			
			getWarnings: function () {
				return this.getList('warnings');
			},
			
			computePitches: function (pitch) {
				return PreciseApi.from(this.getURL('pitches'))
					.traverse(function (builder) {
						return builder.put(pitch);
					});
			}
		
		});

	}
	
	return ModelsService;
	
});