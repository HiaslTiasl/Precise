/**
 * Angular service for loading the diagram data from the server.
 * @module "diagramPaper/DiagramPaper.service"
 */
define([
	'lib/lodash',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape',
	'util/util'
], function (
	_,
	angular,
	joint,
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
		 * Tasks lacking a position will get a random posiiton within
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
			// TODO: consider fetching task types and phases separately and use a
			// task projection that does not include them for reducing duplicate objects
			// that have to be sent and allocated and thus reducing needed traffic, memory and time.
			// The resources have then to be merged into tasks before being returned.
			return $q.all({
				tasks: modelResource.getTasks({ projection: 'expandedTask' })
					.then(mapArrUsing(_.flow(taskToCell, checkPosition))),
				dependencies: modelResource.getDependencies({ projection: 'dependencySummary' })
					.then(mapArrUsing(dependencyToCell))
			}).then(function (cells) {
				return {
					cells: [].concat(cells.tasks, cells.dependencies)
				};
			});
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