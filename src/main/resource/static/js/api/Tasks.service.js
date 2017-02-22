/**
 * Angular service for dealing with task resources.
 * @module "api/Tasks.service"
 */
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
	
	/**
	 * Service constructor
	 * @constructor
	 */
	function TasksService($q, PreciseApi, Pages, Resources, Scopes, OrderSpecifications) {
		
		var Tasks = this;
		
		Tasks.searchSimple = searchSimple;
		Tasks.searchAdvanced = searchAdvanced;
		Tasks.resource = resource;
		Tasks.newResource = newResource;
		Tasks.existingResource = existingResource;
		Tasks.Resource = TaskResource;
		
		var getAttrName = _.property('name'),
			dontSendDirectly = ['id', 'pitch', 'exclusiveness', 'orderSpecifications', 'activity', 'model', '_links'];
		
		/**
		 * Sends the given text as a simple search query to the server
		 * and returns a promise of the resulting tasks as a paged resource.
		 */
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

		/**
		 * Sends the given parameters as an advanced search query to the server
		 * and returns a promise of the resulting tasks as a paged resource.
		 */
		function searchAdvanced(model, params) {
			return PreciseApi.fromBase()
				.traverse(function (builder) {
					return builder
						.follow('tasks', 'search', 'advanced')
						.withTemplateParameters(_.defaults({
							model: PreciseApi.hrefTo(model),
							phase: PreciseApi.hrefTo(params.phase),
							activity: PreciseApi.hrefTo(params.activity),
							craft: PreciseApi.hrefTo(params.craft)
						}, params))
						.get();
				}).then(Pages.wrapper('tasks'));
		}
		
		/** Returns a promise that transforms the given task data to the initial data of the resource. */
		function getData(task, exists) {
			return exists ? cloneExistingData(task) : initializeData(task);
		}
		
		/**
		 * Returns a promise that transforms the given task data
		 * to the initial data of an existing task resource.
		 * The returned data corresponds to the given data extended by
		 * an empty pitch if missing.
		 */
		function cloneExistingData(task) {
			var data = _.cloneDeep(task);
			if (!data.pitch)
				data.pitch = {};
			if (data.activity && data.activity.phase)
				Scopes.rereferenceAttributes(data.exclusiveness, data.activity.phase.attributes);
			return $q.when(data);
		};
		
		/** Returns a promise that transforms the given task data to the initial data of a new resource. */
		function initializeData(task) {
			return $q.when(_.assign({
				activity: null,
				pitch: {
					crewSize: 1,
					crewCount: 1,
				},
				exclusiveness: null,
				orderSpecifications: [],
			}, task));
		};
		
		/** Returns a promise of a new resource that does not exist on the server. */
		function newResource(model, task) {
			return resource(model, task, false);
		}
		
		/** Returns a promise of a resource that already exists on the server. */
		function existingResource(model, task) {
			return resource(model, task, true);
		}
		
		/** Returns a promise of a task resource. */
		function resource(model, task, exists) {
			return getData(task, exists).then(function (data) {
				return new TaskResource(model, data, exists);
			});
		}
		
		/**
		 * Represents a task resource.
		 * @constructor
		 * @extends module:"api/Resources.service"#Base
		 */
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
			
			/**
			 * Asks the server to compute missing pitch parameters
			 * and returns a promise to the result.
			 * If all parameters are specified but inconsistent,
			 * the promise is rejected with a corresponding error.
			 */
			computePitches: function () {
				var self = this,
					pitchData = self.getPitchRequestData();
				return !pitchData ? $q.when() 
					: this.model.computePitches(pitchData)
						.then(function (result) {
							_.assign(self.data, result);
							return self;
						});
			},
			
			/**
			 * Asks the server to adjust the given pattern in terms of allowed values
			 * and returns a promise to the result.
			 */
			checkPattern: function (pattern) {
				return PreciseApi.from(PreciseApi.hrefTo(this.data, 'checkedPattern'))
					.traverse(function (builder) {
						return builder.put(pattern);
					});
			},
			
			/** Returns a promise to a global pattern. */
			globalPattern: function () {
				return this.checkPattern({});
			},
			
			/**
			 * Updates the given attribute to the given value in the given pattern
			 * and returns a promise to the result, which is checked by the server.
			 */
			updatePattern: function (pattern, attr, newValue) {
				// Clone so the original value stays if an error occurs
				var newPattern = _.clone(pattern);	
				newPattern[attr.name].value = newValue;
				return this.checkPattern(newPattern);
			},
			
			/** Returns the data to be sent in a request for the pitch of this task. */
			getPitchRequestData: function () {
				return this.data.pitch && _.pick(this.data.pitch, Boolean);
			},
			
			getRequestData: function () {
				// omit special fields
				var processed = _.omit(this.data, dontSendDirectly),
					pitchData = this.getPitchRequestData();
				// use links for associations to existing resources
				if (pitchData && _.some(pitchData))
					processed.pitch = pitchData;
				if (this.data.exclusiveness)
					processed.exclusiveness = Scopes.toRequestRepresentation(this.data.exclusiveness);
				if (this.data.orderSpecifications)
					processed.orderSpecifications = OrderSpecifications.toRequestRepresentation(this.data.orderSpecifications);
				if (this.data.activity) {
					// Make sure activity is the first property, because the server processes them in order,
					// and locations depend on activity.
					processed = _.defaults({
						activity: HAL.resolve(HAL.hrefTo(this.data.activity))
					}, processed);
				}
				// set model link for new resources
				if (!this.exists)
					processed.model = HAL.resolve(HAL.hrefTo(this.model.data));
				return processed;
			}
			
		});
		
	}
	
	return TasksService;
	
});