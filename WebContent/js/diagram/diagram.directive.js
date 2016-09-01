define([
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape'
],
function (
	joint,
	TaskShape,
	DependencyShape
) {
	'use strict';
	
	diagramDirective.$inject = [];
	
	function diagramDirective() {
		return {
			templateUrl: 'js/diagram/diagram.html',
	        scope: {
	            height: '@',
	            width: '@',
	            gridSize: '@',
	            model: '<',
	            tools: '<'
	        },
	        controller: 'DiagramController',
	        controllerAs: '$ctrl',
	        bindToController: true,
	        link: function (scope, element, attrs) {
	        	var $ctrl = scope.$ctrl,
	        		$paperEl = element.find('.precise-diagram.paper');
	        	
	        	var paper = new joint.dia.Paper({
	                el: $paperEl,
	                width: $ctrl.width || $paperEl.width(),
	                height: $ctrl.height || $paperEl.height(),
	                gridSize: $ctrl.gridSize || 2,
	                model: new joint.dia.Graph(),
	                multiLinks: false,
	                perpendicularLinks: true,
	                defaultConnector: {
	                	name: 'jumpover'
	                },
	                interactive: {
	                	vertexAdd: false,
	                	labelMove: true
	                }
	            });
	        	
	        	scope.$emit('paper:init', paper);

	        	function createParentHighlightHandler(highlighted) {
		        	return function (cellView, el) {
		                var children = cellView.model.getEmbeddedCells();
		                if (children) {
		                	children.forEach(function (c) {
		                		joint.V(paper.findViewByModel(c).el).toggleClass('highlightable', highlighted);
		                	});
		                }
		            };
	        	}
	        	
	            //add event handlers to interact with the diagram
	            paper.on('cell:highlight', createParentHighlightHandler(true));
	            paper.on('cell:unhighlight', createParentHighlightHandler(false));
	            
	            paper.on('blank:pointerclick', function (evt, x, y) {
	                // your logic here e.g. unselect the element by clicking on a blank part of the diagram
	            });
	            paper.on('link:options', function (evt, cellView, x, y) {
	                // your logic here: e.g. select a link by its options tool
	            });
	        } 
	    };
	}
	
	return diagramDirective;
	
});