define([
	'lib/lodash'
],function (
	_
) {
	'use strict';
	
	TaskPropertiesService.$inject = ['$q', 'preciseApi'];
	
	function TaskPropertiesService($q, preciseApi) {
		
		this.getOrderTypes = getOrderTypes;
		this.isAssignableTo = isAssignableTo;
		this.ofResource = ofResource;
		
		var orderTypes = [
			{ name: 'NONE'      , requiresOrdered: false },
			{ name: 'PARALLEL'  , requiresOrdered: false },
			{ name: 'ASCENDING' , requiresOrdered: true },
			{ name: 'DESCENDING', requiresOrdered: true }
		];
		
		function getOrderTypes() {
			return orderTypes;
		}
		
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
			
		function ofResource(taskResource) {
			return new TaskResourceService(taskResource);
		}
		
		function TaskResourceService(taskResource) {
			this.resource = taskResource;
		}
		
		TaskResourceService.prototype.getData = function () {
			var data = _.cloneDeep(this.resource.original()),
				attributes = data.type.phase.attributes,
				excl = data.exclusiveness,
				exclLen = excl.length,
				attrLen = attributes.length;
			for (var exclIdx = 0, attrIdx = 0; exclIdx < exclLen; exclIdx++) {
				var name = excl[exclIdx].name;
				while (attrIdx < attrLen && attributes[attrIdx].name !== name)
					attrIdx++;
				excl[exclIdx] = attributes[attrIdx];
			}
			return $q.resolve(data);
		};
		
		TaskResourceService.prototype.checkPattern = function (pattern) {
			return preciseApi.from(this.resource.link('checkedPattern').href)
				.traverse(function (builder) {
					return builder.put(pattern);
				});
		};
		
		TaskResourceService.prototype.globalPattern = function () {
			return this.checkPattern({});
		};
		
		TaskResourceService.prototype.updatePattern = function (pattern, attr, newValue) {
			var newPattern = _.clone(pattern);
			newPattern[attr.name].value = newValue;
			return this.checkPattern(newPattern);
		};
		
		TaskResourceService.prototype.updateTask = function (data) {
			return preciseApi.from(this.resource.link('task').href)
				.traverse(function (builder) {
					var d = _.omit(data, 'exclusiveness', 'orderSpecifications', 'type', '_links');
					d.exclusiveness = data.exclusiveness.map(function (attr) {
						return attr._links.self.href;
					});
					d.orderSpecifications = data.orderSpecifications.map(function (order, i) {
						return {
							attribute: order.attribute._links.self.href,
							orderType: order.orderType
						};
					});
					return builder
						.withTemplateParameters({
							projection: 'expandedTask'
						})
						.patch(d)
				});
		};
		
		

	}
	
	return TaskPropertiesService;
	
});