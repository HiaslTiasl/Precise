define([
    'lib/lodash',
    'ctrl/DiagramPaper'
], function (
	_,
	DiagramPaper
) {
	'use strict';
	
	DiagramController.$inject = ['$scope', '$rootScope', '$timeout', 'preciseApi', 'preciseDiagram', 'diagramToolset'];
	
	function DiagramController($scope, $rootScope, $timeout, preciseApi, preciseDiagram, diagramToolset) {
		var $ctrl = this;
		
		$ctrl.diagramToolset = diagramToolset;
		
		function broadcast() {
			var args = arguments;
			$timeout(function () {
				$rootScope.$broadcast.apply($rootScope, args);
			});
		}
		
		$scope.$on('paper:init', function (event, paper) {
			$ctrl.diaPaper = new DiagramPaper(paper);
			$ctrl.diaPaper.on('all', broadcast);
			preciseDiagram.toRawGraph($ctrl.model, paper.getArea()).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			});
		});
		
		$scope.$on('properties:cancel', function (event, type) {
			$ctrl.diaPaper.select(type, null);
		});
		
		$scope.$on('properties:change', function (event, type, data) {
			$ctrl.diaPaper.updateSelected(data);
		});
		
		$scope.$on('properties:create', function (event, type, data) {
			$ctrl.diaPaper.create(type, data);
		});
		
	}
	
	return DiagramController;

});