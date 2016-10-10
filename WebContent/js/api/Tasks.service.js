define([
	'lib/lodash',
	'util/util'
],function (
	_,
	util
) {
	'use strict';
	
	TasksService.$inject = ['$q', 'PreciseApi', 'Resources', 'Scopes'];
	
	function TasksService($q, PreciseApi, Resources, Scopes) {
		
		var Tasks = this;
		
		Tasks.isAssignableTo = isAssignableTo;
		Tasks.resource = resource;
		Tasks.newResource = newResource;
		Tasks.existingResource = existingResource;
		Tasks.Resource = Tasks;
		
		Tasks.OrderTypes = {
			NONE      : { name: 'NONE'      , displayName: 'None'            , requiresOrdered: false },
			PARALLEL  : { name: 'PARALLEL'  , displayName: '|Parallel|'      , requiresOrdered: false },
			ASCENDING : { name: 'ASCENDING' , displayName: 'Ascending\u2191' , requiresOrdered: true },
			DESCENDING: { name: 'DESCENDING', displayName: 'Descending\u2193', requiresOrdered: true }
		};

		var getAttrName = _.property('name'),
			dontSendDirectly = ['exclusiveness', 'orderSpecifications', 'type', 'model', '_links'];
		
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
		
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		function cloneExistingData(task) {
			var data = _.cloneDeep(task);
			Scopes.rereferenceAttributes(data.exclusiveness, data.type.phase.attributes);
			return $q.when(data);
		};
		
		function initializeData(task) {
			return $q.when({
				type: null,
				numberOfWorkersNeeded: 0,
				durationDays: 1,
				globalExclusiveness: false,
				exclusiveness: { type: 'NONE' },
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
				processed.exclusiveness = Scopes.toRequestRepresentation(this.data.exclusiveness);
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