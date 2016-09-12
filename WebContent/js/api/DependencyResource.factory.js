define([
	'lib/lodash'
],function (
	_
) {
	'use strict';
	
	dependencyResourceFactory.$inject = ['$q', 'preciseApi'];
	
	function dependencyResourceFactory($q, preciseApi) {
		
		DependencyResource.of = of;
		DependencyResource.ofNew = ofNew;
		DependencyResource.ofExisting = ofExisting;
		
		var dontSendDirectly = ['source', 'target', 'scope', 'attributes', '_links'];
		
		function ofNew(model, dependency) {
			return of(model, dependency, false);
		}
		
		function ofExisting(model, dependency) {
			return of(model, dependency, true);
		}
			
		function of(model, dependency, exists) {
			return getData(dependency, exists).then(function (data) {
				return new DependencyResource(model, data, exists);
			});
		}
		
		function DependencyResource(model, dependency, exists) {
			this.model = model;
			this.dependency = dependency;
			this.exists = exists;
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
			return preciseApi.from(preciseApi.hrefTo(dependency, 'attributes'))
				.followAndGet('attributes[$all]');
		}
		
		DependencyResource.prototype.getRequestData = function () {
			var processed = _.omit(this.dependency, dontSendDirectly);
			if (!this.exists)
				processed.model = preciseApi.hrefTo(this.model);
			else {
				processed.scope = this.dependency.scope.map(function (attr) {
					return preciseApi.hrefTo(attr);
				});
			}
			if (typeof this.dependency.source === 'object')
				processed.source = preciseApi.hrefTo(this.dependency.source);
			if (typeof this.dependency.target === 'object')
				processed.target = preciseApi.hrefTo(this.dependency.target);
			return processed;
		};
		
		DependencyResource.prototype.updateDependency = function () {
			var d = this.getRequestData();
			return preciseApi.from(preciseApi.hrefTo(this.dependency, 'dependency'))
				.traverse(function (builder) {
					return builder
						.withTemplateParameters({
							projection: 'dependencySummary'
						})
						.patch(d)
				});
		};
		
		DependencyResource.prototype.createDependency = function () {
			var d = this.getRequestData();
			return preciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow('dependencies')
						.withTemplateParameters({
							projection: 'dependencySummary'
						})
						.post(d);
				});
		};
		
		DependencyResource.prototype.sendDependency = function () {
			return this.exists ? this.updateDependency() : this.createDependency();
		};
		
		return DependencyResource;

	}
	
	return dependencyResourceFactory;
	
});