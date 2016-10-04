define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	TasksService.$inject = ['$q', 'PreciseApi', 'Resources'];
	
	function TasksService($q, PreciseApi, Resources) {
		
		var Tasks = this;
		
		Tasks.getOrderTypes = getOrderTypes;
		Tasks.isAssignableTo = isAssignableTo;
		Tasks.resource = resource;
		Tasks.newResource = newResource;
		Tasks.existingResource = existingResource;
		Tasks.Resource = Tasks;
		
		var orderTypes = [
			{ name: 'NONE'      , requiresOrdered: false },
			{ name: 'PARALLEL'  , requiresOrdered: false },
			{ name: 'ASCENDING' , requiresOrdered: true },
			{ name: 'DESCENDING', requiresOrdered: true }
		];
		
		var getAttrName = _.property('name'),
			dontSendDirectly = ['exclusiveness', 'orderSpecifications', 'type', 'model', '_links'];
		
		function getOrderTypes() {
			return orderTypes;
		}
		
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
		
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		function cloneExistingData(task) {
			var data = _.cloneDeep(task),
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
			return $q.when(data);
		};
		
		function initializeData(task) {
			return $q.when({
				type: null,
				numberOfWorkersNeeded: 0,
				numberOfUnitsPerDay: 0,
				globalExclusiveness: false,
				exclusiveness: [],
				orderSpecifications: [],
				position: task.position
			});
		};
		
		function newResource(model, task) {
			return resource(model, task, false);
		}
		
		function existingResource(model, task) {
			return resource(model, task, true);
		}
		
		function resource(model, task, exists) {
			return getData(task, exists).then(function (data) {
				return new TaskResource(model, data, exists);
			});
		}
		
		function TaskResource(model, data, exists) {
			Resources.Base.call(this, data, exists);
			this.model = model;
		}
		
		util.defineClass(Resources.Base, {
			
			constructor: TaskResource,
			
			rels: {
				singular: 'task',
				plural: 'tasks'
			},
			
			defaultProjection: 'expandedTask',
			
			showExclusiveness: function () {
				if (!this.data)
					return undefined;
				if (!this.exclParts)
					this.exclParts = [];
				util.mapInto(this.exclParts, this.data.exclusiveness, getAttrName);
				return !this.exclParts.length ? null : this.exclParts.join(', ');
			},
			
			showOrderPart: function (order) {
				var attrName = order.attribute.name;
				switch (order.orderType) {
				case 'PARALLEL':
					return '|' + attrName + '|';
				case 'ASCENDING':
					return attrName + '\u2191'; 	// ↑
				case 'DESCENDING':
					return attrName + '\u2193'; 	// ↓
				default:
					return null;
				}
			},
			
			showOrder: function () {
				if (!this.data)
					return undefined;
				if (!this.orderParts)
					this.orderParts = [];
				var count = 0;
				this.data.orderSpecifications.forEach(function (order) {
					var str = this.showOrderPart(order);
					if (str != null)
						this.orderParts[count++] = str;
				}, this);
				util.limitArray(this.orderParts, count);
				return !this.orderParts.length ? null : this.orderParts.join(', ');
			},
			
			updateExlusiveness: function () {
				if (this.data.globalExclusiveness)
					util.limitArray(this.data.exclusiveness, 0);
			},
			
			updateGlobalExclusiveness: function () {
				if (this.data.exclusiveness.length)
					this.data.globalExclusiveness = false;
			},
			
			checkPattern: function (pattern) {
				return PreciseApi.from(PreciseApi.hrefTo(this.data, 'checkedPattern'))
					.traverse(function (builder) {
						return builder.put(pattern);
					});
			},
			
			globalPattern: function () {
				return this.checkPattern({});
			},
			
			updatePattern: function (pattern, attr, newValue) {
				var newPattern = _.clone(pattern);
				newPattern[attr.name].value = newValue;
				return this.checkPattern(newPattern);
			},
			
			getRequestData: function () {
				var processed = _.omit(this.data, dontSendDirectly);
				processed.exclusiveness = this.data.exclusiveness.map(function (attr) {
					return PreciseApi.hrefTo(attr);
				});
				processed.orderSpecifications = this.data.orderSpecifications.map(function (order, i) {
					return {
						attribute: PreciseApi.hrefTo(order.attribute),
						orderType: order.orderType
					};
				});
				if (!this.exists) {
					processed.type = PreciseApi.hrefTo(this.data.type);
					processed.model = PreciseApi.hrefTo(this.model.data);
				}
				return processed;
			}
			
		});
		
	}
	
	return TasksService;
	
});