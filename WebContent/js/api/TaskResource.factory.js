define([
	'lib/lodash'
],function (
	_
) {
	'use strict';
	
	taskResourceFactory.$inject = ['$q', 'preciseApi'];
	
	function taskResourceFactory($q, preciseApi) {
		
		TaskResource.getOrderTypes = getOrderTypes;
		TaskResource.isAssignableTo = isAssignableTo;
		TaskResource.of = of;
		TaskResource.ofNew = ofNew;
		TaskResource.ofExisting = ofExisting;
		TaskResource.Resource = TaskResource;
		
		var orderTypes = [
			{ name: 'NONE'      , requiresOrdered: false },
			{ name: 'PARALLEL'  , requiresOrdered: false },
			{ name: 'ASCENDING' , requiresOrdered: true },
			{ name: 'DESCENDING', requiresOrdered: true }
		];
		
		var dontSendDirectly = ['exclusiveness', 'orderSpecifications', 'type', 'model', '_links'];
		
		function getOrderTypes() {
			return orderTypes;
		}
		
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
		
		function ofNew(model, task) {
			return of(model, task, false);
		}
		
		function ofExisting(model, task) {
			return of(model, task, true);
		}
		
		function of(model, task, exists) {
			return getData(task, exists).then(function (data) {
				return new TaskResource(model, data, exists);
			});
		}
		
		function TaskResource(model, task, exists) {
			this.model = model;
			this.task = task;
			this.exists = exists;
		}
		
		TaskResource.prototype.getPhases = function () {
			return preciseApi.from(preciseApi.hrefTo(this.model, 'phases'))
				.followAndGet('phases[$all]');
		};
		
		TaskResource.prototype.getTaskTypes = function (phase) {
			return preciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow('taskTypes', 'search', 'findByPhase', 'taskTypes[$all]')
						.withTemplateParameters({
							phase: preciseApi.hrefTo(phase)
						})
						.get();
				});
		};
		
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
		
		TaskResource.prototype.checkPattern = function (pattern) {
			return preciseApi.from(preciseApi.hrefTo(this.task, 'checkedPattern'))
				.traverse(function (builder) {
					return builder.put(pattern);
				});
		};
		
		TaskResource.prototype.globalPattern = function () {
			return this.checkPattern({});
		};
		
		TaskResource.prototype.updatePattern = function (pattern, attr, newValue) {
			var newPattern = _.clone(pattern);
			newPattern[attr.name].value = newValue;
			return this.checkPattern(newPattern);
		};
		
		TaskResource.prototype.getRequestData = function () {
			var processed = _.omit(this.task, dontSendDirectly);
			processed.exclusiveness = this.task.exclusiveness.map(function (attr) {
				return preciseApi.hrefTo(attr);
			});
			processed.orderSpecifications = this.task.orderSpecifications.map(function (order, i) {
				return {
					attribute: preciseApi.hrefTo(order.attribute),
					orderType: order.orderType
				};
			});
			if (!this.exists) {
				processed.type = preciseApi.hrefTo(this.task.type);
				processed.model = preciseApi.hrefTo(this.model);
			}
			return processed;
		};
		

		TaskResource.prototype.createTask = function () {
			var d = this.getRequestData();
			return preciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow('tasks')
						.withTemplateParameters({
							projection: 'expandedTask'
						})
						.post(d);
				});
		};
		
		TaskResource.prototype.updateTask = function () {
			var d = this.getRequestData();
			return preciseApi.from(preciseApi.hrefTo(this.task, 'task'))
				.traverse(function (builder) {
					return builder
						.withTemplateParameters({
							projection: 'expandedTask'
						})
						.patch(d)
				});
		};
		
		TaskResource.prototype.sendTask = function () {
			return this.exists ? this.updateTask() : this.createTask();
		};
		
		return TaskResource;

	}
	
	return taskResourceFactory;
	
});