define([
	'lib/lodash',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/LocationShape',
	'shapes/DependencyShape'
], function (
	_,
	angular,
	joint,
	TaskShape,
	LocationShape,
	DependencyShape
) {
	'use strict';
	
	DiagramService.$inject = ['$q', 'preciseApi'];
	
	function DiagramService($q, preciseApi) {
		
		this.toRawGraph = toRawGraph;
		
		function toRawGraph(model, bbox) {
			
			var randomBBox = _.defaults({
				width: bbox.width - TaskShape.WIDTH,
				height: bbox.height - 4 * TaskShape.DEFAULT_HEIGHT
			}, bbox);
			
			function checkPosition(task) {
				if (!task.position)
					task.position = randomPosition(randomBBox);
				return task;
			}
			
			var baseResource = preciseApi.fromBase(),
				modelHref = model.link('self').href;
		
			return $q.all({
				tasks: baseResource
					.traverse(function (builder) {
						// Use 'search' method since projections are not exposed in associations
						return builder.follow('tasks', 'search', 'findByModel', 'tasks[$all]')
							.withTemplateParameters({
								model: modelHref,
								projection: 'fullTask'
							})
							.get();
					})
					.then(mapArrUsing(_.flow(taskToCell, checkPosition))),
				locations: baseResource
					.traverse(function (builder) {
						return builder
							.follow(
								'locations',
								'search',
								'findByTask_Model',
								'locations[$all]'
							)
							.withTemplateParameters({
								model: modelHref
							})
							.get();
					})
					.then(mapArrUsing(locationToCell)),
				dependencies: baseResource
					.traverse(function (builder) {
						return builder
							.follow('dependencies', 'search', 'findByModel', 'dependencies[$all]')
							.withTemplateParameters({
								model: modelHref,
								projection: 'dependencySummary'
							})
							.get()
					})
					.then(mapArrUsing(dependencyToCell))
			}).then(function (cells) {
				return {
					cells: [].concat(cells.tasks, cells.locations, cells.dependencies)
				};
			});
		}
		
		function taskToCell(task) {
			return {
				id: TaskShape.toTaskID(task.id),
				type: 'precise.TaskShape',
				position: task.position,
				embeds: [],
				data: task
			};
		}
		
		function locationToCell(location) {
			return {
				id: LocationShape.toLocationID(location.id),
				type: 'precise.LocationShape',
				parent: TaskShape.toTaskID(location.taskID),
				data: location
			};
		}
		
		function dependencyToCell(dependency) {
			return {
				id: DependencyShape.toDependencyID(dependency.id),
				type: 'precise.DependencyShape',
				source: { id: TaskShape.toTaskID(dependency.sourceID) },
				target: { id: TaskShape.toTaskID(dependency.targetID) },
				vertices: dependency.vertices,
				//labels: []		// TODO check whether required
				data: dependency
			};
		}
		
		function mapArrUsing(mapper) {
			return function (arr) {
				return arr.map(mapper);
			};
		}
		
		function randomPosition(bbox) {
			return {
				x: getRandomIntInclusive(bbox.x, bbox.x + bbox.width),
				y: getRandomIntInclusive(bbox.y, bbox.y + bbox.height)
			}
		}
		
		// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/random
		// Returns a random integer between min (included) and max (included)
		// Using Math.round() will give you a non-uniform distribution!
		function getRandomIntInclusive(min, max) {
			min = Math.ceil(min);
			max = Math.floor(max);
			return Math.floor(Math.random() * (max - min + 1)) + min;
		}
	}
	
	return DiagramService;
	
});