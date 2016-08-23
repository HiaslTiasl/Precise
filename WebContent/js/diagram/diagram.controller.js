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
		
		$scope.$on('paper:init', function (event, paper) {
			$ctrl.diaPaper = new DiagramPaper(paper);
			$ctrl.diaPaper.on('all', function () {
				var args = arguments;
				$timeout(function () {
					$rootScope.$broadcast.apply($rootScope, args);
				});
			});
			preciseDiagram.toRawGraph($ctrl.model, paper.getArea()).then(function (rawGraph) {
				$ctrl.diaPaper.fromJSON(rawGraph);
			});
		});
		
	}
	
	return DiagramController;

});