/**
 * Angular service for dealing with model resources.
 * @module "api/Models.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	ModelsService.$inject = ['PreciseApi', 'Resources', 'Pages'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
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
		
		/** Returns a promise of the first page of models. */
		function firstPage(templateParams) {
			return PreciseApi.fromBase().traverse(function (builder) {
				return builder
					.follow(REL_PLURAL)
					.withTemplateParameters(templateParams)
					.get();
			}).then(Pages.wrapper(REL_PLURAL));
		}
		
		/** Returns a promise of the list of all models. */
		function findAll(templateParams) {
			return firstPage(templateParams)
				.then(function (page) {
					return page.collectRemaining(templateParams);
				});
		}
		
		/**
		 * Returns a promise of the model of the specified name.
		 * Accepts additional template parameters (e.g. for projections).
		 */
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
		
		/**
		 * Returns a promise of the model of the specified name and includes
		 * state information in the result.
		 */
		function findByNameWithPartInfos(name) {
			return findByName(name, {
				projection: 'modelSummary'
			});
		}
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model) {
			return resource(model, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model) {
			return resource(model, true);
		}
		
		/** Returns a promise of a model resource. */
		function resource(model, existing) {
			return new ModelResource(model, existing);
		}
		
		/** 
		 * Represents a model resource.
		 * @constructor
		 * @extends module:"api/Resource.service"#Base
		 */
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
			
			computePitches: function (pitchData) {
				return PreciseApi.from(this.getURL('pitches'))
					.traverse(function (builder) {
						return builder.put(pitchData);
					});
			},
		
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
			}
		
		});

	}
	
	return ModelsService;
	
});