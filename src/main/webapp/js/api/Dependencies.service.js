/**
 * Angular service for dealing with dependency resources. 
 * @module "api/Crafts.service"
 */
define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependenciesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Scopes', 'Pages'];
	
	/** @constructor */
	function DependenciesService($q, PreciseApi, Resources, Scopes, Pages) {
		
		var Dependencies = this;
		
		Dependencies.resource = resource;
		Dependencies.newResource = newResource;
		Dependencies.existingResource = existingResource;
		Dependencies.Resource = DependencyResource;
		
		var dontSendDirectly = ['source', 'target', 'scope', 'attributes', '_links'];
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, dependency) {
			return resource(model, dependency, false);
		}
		
		/** Returns a promise of resource that already exists on the server. */
		function existingResource(model, dependency) {
			return resource(model, dependency, true);
		}
			
		/** Returns a promise of a new dependency resource. */
		function resource(model, dependency, exists) {
			return getData(dependency, exists).then(function (data) {
				return new DependencyResource(model, data, exists);
			});
		}
		
		/** Returns a promise that transforms the given task data to the initial data of the resource. */
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		/**
		 * Returns a promise that transforms the given dependency data
		 * to the initial data of an existing dependency resource.
		 * The returned data corresponds to the given data extended by,
		 * the source task, the target task, and the list of allowed attributes,
		 * all obtained from the server.
		 */
		function cloneExistingData(dependency) {
			var data = _.cloneDeep(dependency);
			return $q.all({
				attributes: allowedAttributes(dependency),
				source: getSource(dependency),
				target: getTarget(dependency)
			}).then(function (results) {
				// Ensure same references are used for scope
				Scopes.rereferenceAttributes(data.scope, results.attributes);
				// Only assign available data to avoid resetting it accidentally
				return _.assign(data, _.pick(results, Boolean));
			});
		}
		
		/** Returns a promise that transforms the given dependency data to the initial data of a new resource. */
		function initializeData(dependency) {
			// Use the given data as-is.
			return $q.when(dependency);
		}
		
		/**
		 * Returns the a promise of the attributes allowed for the given dependency,
		 * obtained from the server
		 */
		function allowedAttributes(dependency) {
			return PreciseApi.from(PreciseApi.hrefTo(dependency, 'attributes'))
				.followAndGet()
				.then(Pages.wrapper('attributes'))
				.then(Pages.collectRemaining);
		}
		
		/**
		 * Returns a promise to the (expanded) tasks identified by given URL.
		 * If href is null, the promise is resolved with null.
		 * Otherwise it is resolved with the data from the server. 
		 */
		function getTask(href) {
			return !href ? $q.when(null)
				: PreciseApi.from(href)
					.traverse(function (builder) {
						return builder
							.withTemplateParameters({
								projection: 'expandedTask'
							})
							.get();
					});
		}
		
		/** Returns a promise to the source task of the given dependency as an (expanded) task. */
		function getSource(dependency) {
			return getTask(PreciseApi.hrefTo(dependency.source));
		}
		
		/** Returns a promise to the target task of the given dependency as an (expanded) task. */
		function getTarget(dependency) {
			return getTask(PreciseApi.hrefTo(dependency.target));
		}
		
		/**
		 * Represents a dependency resource.
		 * @constructor
		 * @extends module:"api/Resource.service"#Base
		 */
		function DependencyResource(model, data, exists) {
			Resources.Base.call(this, data, exists);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: DependencyResource,

			rels: {
				singular: 'dependency',
				plural: 'dependencies'
			},
			
			defaultProjection: 'dependencySummary',
			
			getRequestData: function () {
				// omit special fields
				var processed = _.omit(this.data, dontSendDirectly);
				// set model link for new resources
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				// use links for associations to existing resources
				if (typeof this.data.scope === 'object')
					processed.scope = Scopes.toRequestRepresentation(this.data.scope);
				if (typeof this.data.source === 'object')
					processed.source = PreciseApi.hrefTo(this.data.source);
				if (typeof this.data.target === 'object')
					processed.target = PreciseApi.hrefTo(this.data.target);
				return processed;
			}
			
		});
		
	}
	
	return DependenciesService;
	
});