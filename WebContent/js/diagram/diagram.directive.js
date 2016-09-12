define([
	'svg-pan-zoom',
	'lib/lodash',
	'lib/hammer',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape'
],
function (
	svgPanZoom,
	_,
	Hammer,
	angular,
	joint,
	TaskShape,
	DependencyShape
) {
	'use strict';
	
	var GUTTER_WIDTH = 100;
	
	diagramDirective.$inject = ['$window', '$timeout'];
	
	function diagramDirective($window, $timeout) {
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
	        		$paperEl = element.find('.precise-diagram.paper'),
	        		model = new joint.dia.Graph();
	        	
	        	$ctrl.enablePan = enablePan;
	        	$ctrl.disablePan = disablePan;
	        	$ctrl.fitAndCenter = fitAndCenter;
	        	
	        	var paper = new joint.dia.Paper({
	                el: $paperEl,
	                width: $ctrl.width || $paperEl.width(),
	                height: $ctrl.height || $paperEl.height(),
	                gridSize: $ctrl.gridSize || 10,
	                model: model,
	                multiLinks: false,
	                perpendicularLinks: true,
	                async: true,
	                defaultConnector: {
	                	name: 'jumpover'
	                },
	                interactive: {
	                	vertexAdd: false,
	                	labelMove: true
	                }
	            });
	        	
	        	// Setup pan and zoom functionality
	        	// See http://plnkr.co/edit/djYRygTGnQOvaBICk1dE?p=preview
	        	var paperPanZoom = svgPanZoom(paper.svg, {
					viewportSelector: paper.viewport,
					fit: false,
					contain: false,
					center: false,
					zoomScaleSensitivity: 0.1,
					panEnabled: false,
					beforePan: beforePan,
					onZoom: wrapInTimeout(onZoom),
					// http://ariutta.github.io/svg-pan-zoom/demo/mobile.html
					customEventsHandler: {
			        	init: function (options) {
			        		var instance = options.instance,
				        		pinchCenter;
			        		// Init Hammer
			        		// Listen only for pointer and touch events
			        		this.hammer = new Hammer.Manager(paper.svg, {
			        			inputClass: Hammer.SUPPORT_POINTER_EVENTS ? Hammer.PointerEventInput : Hammer.TouchInput
			        		});
			        		this.hammer.add(new Hammer.Pinch({ enable: true }));
			        		// Handle pinch
			        		this.hammer.on('pinchstart pinchmove', function (ev) {
			        			if (ev.type === 'pinchstart') {
			        				pinchCenter = ev.center;
			        				instance.disablePan();
			        			}
			        			instance.zoomAtPointBy(ev.scale, pinchCenter);
			        		});
			        		// Prevent moving the page on some devices when panning over SVG
			        		paper.svg.addEventListener('touchmove', function (e) {
			        			e.preventDefault();
			        		});
			        	},
			        	destroy: function() {
			        		this.hammer.destroy();
			        	}
					}
				});
				
				//Enable pan when a blank area is click (held) on
				paper.on('blank:pointerdown', enablePan);
				//Disable pan when the mouse button is released
				paper.on('cell:pointerup blank:pointerup', disablePan);
				
				scope.$on('render:done', initialPanAndZoom);
				scope.$on('diagram:change', updateBBox);
				model.on('add remove', wrapInTimeout(updateBBox));
				
				function wrapInTimeout(fn) {
					return _.partial($timeout, fn, 0, true);
				}
				
				function beforePan(oldPan, newPan) {
					var sizes = paperPanZoom.getSizes(),
						xMin = -sizes.viewBox.x * sizes.realZoom - GUTTER_WIDTH, 
						xMax = sizes.width - ((sizes.viewBox.x + sizes.viewBox.width) * sizes.realZoom) + GUTTER_WIDTH,
						yMin = -sizes.viewBox.y * sizes.realZoom - GUTTER_WIDTH,
						yMax = sizes.height - ((sizes.viewBox.y + sizes.viewBox.height) * sizes.realZoom) + GUTTER_WIDTH,
		          		xOK = _.inRange(newPan.x, xMin, xMax),
		          		yOK = _.inRange(newPan.y, yMin, yMax);
		          		
		          return xOK === yOK ? xOK : { x: xOK, y: yOK };
				}
				
				function initialPanAndZoom() {
					updateBBox();
					fitAndCenter()
				}
				
				function enablePan() {
					paperPanZoom.enablePan();
				}
				
				function disablePan() {
					paperPanZoom.disablePan();
				}
				
				function updateBBox() {
					paperPanZoom.updateBBox();
				}
				
				function fitAndCenter() {
					paperPanZoom.fit();
					paperPanZoom.center();
				}
				
				function onZoom(scale) {
					$ctrl.zoomScale = scale;
				}
				
				$ctrl.onPaperInit(paper);
	        	
	        	// TODO: Either find a better way or don't support key shortcuts.
	        	// the following effectively captures events, but in too many cases.
	        	// For example, when the user presses `Del` while editing a task property,
	        	// the `keyup` event is captured and the task is deleted.
	        	
	        	// One options: 
	        	
//	        	angular.element($window).on('keyup', function (event) {
//	        		scope.$on('keyup', function (event) {
//		        		switch (event.keyCode) {
//		    			case 46:	// Del
//		    				var view = $ctrl.diaPaper.selectedView;
//		    				if (view)
//		    					scope.$emit('cell:delete', $ctrl.diaPaper.selectedNS, view.model.get('data'))
//		    				break;
//		    			case 26:	// Esc
//		    				$ctrl.diaPaper.resetEditMode();
//		    				break;
//		    			}
//		        	});
	        } 
	    };
	}
	
	return diagramDirective;
	
});