define([
	'lib/lodash',
	'api/hal',
	'util/util'
],function (
	_,
	HAL,
	util
) {
	'use strict';
	
	TasksService.$inject = ['$q', 'PreciseApi', 'Pages', 'Resources', 'Scopes', 'OrderSpecifications'];
	
	function TasksService($q, PreciseApi, Pages, Resources, Scopes, OrderSpecifications) {
		
		var Tasks = this;
		
		Tasks.searchSimple = searchSimple;
		Tasks.searchAdvanced = searchAdvanced;
		Tasks.resource = resource;
		Tasks.newResource = newResource;
		Tasks.existingResource = existingResource;
		Tasks.Resource = Tasks;
		

		var getAttrName = _.property('name'),
			dontSendDirectly = ['exclusiveness', 'orderSpecifications', 'type', 'model', '_links'];
		
		function searchSimple(model, text) {
			return PreciseApi.fromBase()
			.traverse(function (builder) {
				return builder
					.follow('tasks', 'search', 'simple')
					.withTemplateParameters({
						model: PreciseApi.hrefTo(model),
						q: text
					})
					.get();
			}).then(Pages.wrapper('tasks'));
		}

		function searchAdvanced(model, params) {
			return PreciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow('tasks', 'search', 'advanced')
						.withTemplateParameters(_.defaults({
							model: PreciseApi.hrefTo(model),
							phase: PreciseApi.hrefTo(params.phase),
							type: PreciseApi.hrefTo(params.type),
							craft: PreciseApi.hrefTo(params.craft)
						}, params))
						.get();
				}).then(Pages.wrapper('tasks'));
		}
		
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		function cloneExistingData(task) {
			var data = _.cloneDeep(task);
			if (data.type.phase)
				Scopes.rereferenceAttributes(data.exclusiveness, data.type.phase.attributes);
			return $q.when(data);
		};
		
		function initializeData(task) {
			return $q.when({
				type: null,
				durationType: 'AUTO',
				totalQuantity: 0,
				quantityPerDay: 0,
				crewSize: 1,
				crewCount: 1,
				exclusiveness: null,
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
				processed.orderSpecifications = OrderSpecifications.toRequestRepresentation(this.data.orderSpecifications);
				if (this.data.type)
					processed.type = HAL.resolve(HAL.hrefTo(this.data.type));
				if (!this.exists)
					processed.model = HAL.resolve(HAL.hrefTo(this.model.data));
				return processed;
			}
			
		});
		
	}
	
	return TasksService;
	
});