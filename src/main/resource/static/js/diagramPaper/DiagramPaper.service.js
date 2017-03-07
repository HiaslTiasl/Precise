/**
 * Angular service for loading the diagram data from the server.
 * @module "diagramPaper/DiagramPaper.service"
 */
define([
	'lib/lodash',
	'lib/angular',
	'lib/joint',
	'api/hal',
	'shapes/TaskShape',
	'shapes/DependencyShape',
	'util/util'
], function (
	_,
	angular,
	joint,
	HAL,
	TaskShape,
	DependencyShape,
	util
) {
	'use strict';
	
	DiagramService.$inject = ['$q', 'PreciseApi', 'Pages'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function DiagramService($q, PreciseApi, Pages) {
		
		this.toRawGraph = toRawGraph;
		
		/**
		 * Loads the diagram data from the given model resource
		 * and returns it as a JSON object as expected by JointJS.
		 * Tasks lacking a position will get a random position within
		 * the given bbox.
		 */
		function toRawGraph(modelResource, bbox) {
			// Take into account task dimensions since positions refer to the upper-left corner.
			var randomBBox = _.defaults({
				width: bbox.width - TaskShape.WIDTH,
				height: bbox.height - 4 * TaskShape.DEFAULT_HEIGHT
			}, bbox);
			
			/** Checks whether the given task has a position and sets a random one if needed. */
			function checkPosition(task) {
				if (!task.position)
					task.position = randomPosition(randomBBox);
				return task;
			}
			
			// Fetch tasks and dependencies, then merge them into one cell array.
			// TODO: Decide whether to join remotely or locally.
			// Joining remotely is easier and requires less processing on the client,
			// while joining locally likely leads to less data transmissions and heap allocations.
			//
			// Experiments on thesis-examples.mdl (measuring transmission only!):
			// 
			//   remotely: ~400 ms, 198.45 KB
			//   locally:  ~350 ms, 143.73 KB
			
			return $q.all({
				tasks: fetchTasksJoinRemotely(modelResource)
					.then(mapArrUsing(_.flow(taskToCell, checkPosition))),
				dependencies: fetchDependencies(modelResource)
					.then(mapArrUsing(dependencyToCell))
			}).then(function (cells) {
				return {
					cells: [].concat(cells.tasks, cells.dependencies)
				};
			});
		}
		
		/** Fetches tasks using a predefined projection containing all the necessary data. */
		function fetchTasksJoinRemotely(modelResource) {
			return modelResource.getTasks({ projection: 'expandedTask' })
		}
		
		/**
		 * Fetches tasks using projections with minimal data for tasks, activities, and phases,
		 * and join them locally using self links.
		 */
		function fetchTasksJoinLocally(modelResource) {
			// Fetch activities and phases, and index them independently
			var indexedActivitiesPromise = $q.all({
				indexedPhases: modelResource.getPhases({ projection: 'phaseSummary' })
					.then(indexBySelfLink),
					indexedActivities: modelResource.getActivities({ projection: 'activitySummary' })
					.then(indexBySelfLink)
			}).then(function (results) {
				// join
				_.forEach(results.indexedActivities, function (activity) {
					activity.phase = results.indexedPhases[getSelfLink(activity.phase)];
				});
				return results.indexedActivities;
			});
			
			// Fetch tasks themselves and join them to types
			return $q.all({
				indexedActivities: indexedActivitiesPromise,
				tasks: modelResource.getTasks({ projection: 'taskSummary' })
			}).then(function (results) {
				_.forEach(results.tasks, function (task) {
					task.activity = results.indexedActivities[getSelfLink(task.activity)];
				});
				return results.tasks;
			});
		}
		
		/** Fetches dependencies. */
		function fetchDependencies(modelResource) {
			return modelResource.getDependencies({ projection: 'dependencySummary' });
		}
		
		/** Returns the self link of the given resource. */
		function getSelfLink(resource) {
			return HAL.resolve(HAL.hrefTo(resource));
		}
		
		/** Returns the given resources as a map where self links are used as keys. */
		function indexBySelfLink(resources) {
			return _.indexBy(resources, getSelfLink);
		}
		
		/** Transform the given task to JSON cell data. */
		function taskToCell(task) {
			return {
				id: PreciseApi.hrefTo(task),
				type: TaskShape.prototype.defaults.type,
				data: task
			};
		}
		
		/** Transform the given dependency to JSON cell data. */
		function dependencyToCell(dependency) {
			return {
				id: PreciseApi.hrefTo(dependency),
				type: DependencyShape.prototype.defaults.type,
				data: dependency
			};
		}
		
		/** Creates a function that accepts an array and maps it to a new array using the given mapper. */
		function mapArrUsing(mapper) {
			return function (arr) {
				return arr.map(mapper);
			};
		}
		
		/** Returns a random position within the given bounding box. */
		function randomPosition(bbox) {
			return {
				x: util.getRandomIntIncl(bbox.x, bbox.x + bbox.width),
				y: util.getRandomIntIncl(bbox.y, bbox.y + bbox.height)
			}
		}
		
	}
	
	return DiagramService;
	
});