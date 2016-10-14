define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependenciesService.$inject = ['$q', 'PreciseApi', 'Resources', 'Scopes'];
	
	function DependenciesService($q, PreciseApi, Resources, Scopes) {
		
		var Dependencies = this;
		
		Dependencies.resource = resource;
		Dependencies.newResource = newResource;
		Dependencies.existingResource = existingResource;
		Dependencies.Resource = DependencyResource;
		
		var dontSendDirectly = ['source', 'target', 'scope', 'attributes', '_links'];
		
		function newResource(model, dependency) {
			return resource(model, dependency, false);
		}
		
		function existingResource(model, dependency) {
			return resource(model, dependency, true);
		}
			
		function resource(model, dependency, exists) {
			return getData(dependency, exists).then(function (data) {
				return new DependencyResource(model, data, exists);
			});
		}
		
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		function cloneExistingData(dependency) {
			var data = _.cloneDeep(dependency);
			return $q.all([
				allowedAttributes(dependency).then(function (attributes) {
					data.attributes = attributes;
					Scopes.rereferenceAttributes(data.scope, attributes);
					return data;
				}),
				getSource(dependency).then(function (source) {
					data.source = source;
				}),
				getTarget(dependency).then(function (target) {
					data.target = target;
				})
			]).then(function () {
				return data;
			});
		}
		
		function initializeData(dependency) {
			return $q.when(dependency);
		}
		
		function allowedAttributes(dependency) {
			return PreciseApi.from(PreciseApi.hrefTo(dependency, 'attributes'))
				.followAndGet('attributes[$all]');
		}
		
		function getTask(data) {
			return !data ? $q.when(null)
				: PreciseApi.from(PreciseApi.hrefTo(data))
					.traverse(function (builder) {
						return builder
							.withTemplateParameters({
								projection: 'expandedTask'
							})
							.get();
					});
		}
		
		function getSource(dependency) {
			return getTask(dependency.source);
		}
		
		function getTarget(dependency) {
			return getTask(dependency.target);				
		}
		
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
				var processed = _.omit(this.data, dontSendDirectly);
				if (!this.exists)
					processed.model = PreciseApi.hrefTo(this.model.data);
				else
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