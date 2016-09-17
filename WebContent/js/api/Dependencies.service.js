define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	DependenciesService.$inject = ['$q', 'PreciseApi', 'Resources'];
	
	function DependenciesService($q, PreciseApi, Resources) {
		
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
			return allowedAttributes(dependency).then(function (attributes) {
				var scope = data.scope || (data.scope = []),
				scopeLen = scope.length,
				attrLen = attributes.length;
				for (var scopeIdx = 0, attrIdx = 0; scopeIdx < scopeLen; scopeIdx++) {
					var s = scope[scopeIdx];
					if (s != null) {
						while (attrIdx < attrLen && attributes[attrIdx].name !== s.name)
							attrIdx++;
						scope[scopeIdx] = attributes[attrIdx];
					}
				}
				data.attributes = attributes;
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
				else {
					processed.scope = this.data.scope.map(function (attr) {
						return PreciseApi.hrefTo(attr);
					});
				}
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