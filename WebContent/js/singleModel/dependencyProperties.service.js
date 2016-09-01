define([
	'lib/lodash'
],function (
	_
) {
	'use strict';
	
	DependencyResourceService.$inject = ['preciseApi'];
	
	function DependencyResourceService(preciseApi) {
		
		this.ofResource = ofResource;
			
		function ofResource(taskResource) {
			return new DependencyResourceService(taskResource);
		}
		
		function DependencyResourceService(taskResource) {
			this.resource = taskResource;
		}
		
		DependencyResourceService.prototype.getData = function () {
			var data = _.cloneDeep(this.resource.original());
			return this.getAllowedAttributes().then(function (attributes) {
				var scope = data.scope,
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
		};
		
		DependencyResourceService.prototype.getAllowedAttributes = function () {
			return preciseApi.from(this.resource.link('attributes').href)
				.followAndGet('attributes[$all]');
		};
		
		DependencyResourceService.prototype.updateDependency = function (data) {
			return preciseApi.from(this.resource.link('dependency').href)
				.traverse(function (builder) {
					var d = _.omit(data, 'scope', 'attributes', '_links');
					d.scope = data.scope.map(function (attr) {
						return attr.link('self').href;
					});
					return builder
						.withTemplateParameters({
							projection: 'dependencySummary'
						})
						.patch(d)
				});
		};

	}
	
	return DependencyResourceService;
	
});