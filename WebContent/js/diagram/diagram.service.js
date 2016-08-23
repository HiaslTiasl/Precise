define([
	'lib/lodash',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape'
], function (
	_,
	angular,
	joint,
	TaskShape
) {
	'use strict';
	
	DiagramService.$inject = ['$q', 'preciseApi'];
	
	function DiagramService($q, preciseApi) {
		
		this.toRawGraph = toRawGraph;
		
		function toRawGraph(model, bbox) {
			
			bbox.width -= TaskShape.WIDTH;
			bbox.height -= TaskShape.HEIGHT;
		
			function taskToCell(task) {
				return {
					id: String(task.id),
					type: 'precise.TaskShape',
					position: task.position || randomPosition(bbox),
					embeds: [],
					data: task
				};
			}
			
			function locationToCell(location) {
				return {
					id: String(location.id),
					type: 'precise.LocationShape',
					parent: String(location.taskID),
					data: location
				};
			}
			
			function dependencyToCell(dependency) {
				return {
					id: String(dependency.id),
					type: 'precise.DependencyShape',
					source: { id: String(dependency.sourceID) },
					target: { id: String(dependency.targetID) },
					vertices: dependency.vertices,
					//labels: []		// TODO check whether required
					data: dependency
				};
			}
			
			return $q.all({
				tasks: preciseApi.from(model.link('tasks').href)
					.followAndGet('tasks[$all]')
					.then(mapArrUsing(taskToCell)),
				locations: preciseApi.fromBase()
					.traverse(function (builder) {
						return builder
							.follow(
								'locations',
								'search',
								'findByTask_Model',
								'locations[$all]'
							)
							.withTemplateParameters({
								model: model.link('self').href
							})
							.get();
					})
					.then(mapArrUsing(locationToCell)),
				dependencies: preciseApi.from(model.link('dependencies').href)
					.followAndGet('dependencies[$all]')
					.then(mapArrUsing(dependencyToCell))
			}).then(function (cells) {
//				var tasksByID = _.indexBy(cells.tasks, 'id');
//				cells.locations.forEach(function (loc) {
//					tasksByID[loc.parent].embeds.push(loc.id);
//				});
				return {
					cells: [].concat(cells.tasks, cells.locations, cells.dependencies)
				};
			});
			
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