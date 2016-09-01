define([
	'lib/lodash',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape'
], function (
	_,
	angular,
	joint,
	TaskShape,
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
		
			// Use 'search' method since projections are not exposed in associations
			return $q.all({
				tasks: baseResource
					.traverse(function (builder) {
						return builder.follow('tasks', 'search', 'findByModel', 'tasks[$all]')
							.withTemplateParameters({
								model: modelHref,
								projection: 'expandedTask'
							})
							.get();
					})
					.then(mapArrUsing(_.flow(taskToCell, checkPosition))),
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
					cells: [].concat(cells.tasks, cells.dependencies)
				};
			});
		}
		
		function taskToCell(task) {
			return {
				id: TaskShape.toTaskID(task.id),
				type: 'precise.TaskShape',
				position: task.position,
				data: task
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