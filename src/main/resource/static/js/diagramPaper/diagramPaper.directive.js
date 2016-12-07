define([
	'jquery',
	'svg-pan-zoom',
	'lib/lodash',
	'lib/hammer',
	'lib/angular',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape'
],
function (
	$,
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
	
	diagramPaperDirective.$inject = ['$window', '$timeout'];
	
	function diagramPaperDirective($window, $timeout) {
		return {
			templateUrl: 'js/diagramPaper/diagramPaper.html',
	        scope: {
	            height: '@',
	            width: '@',
	            gridSize: '@',
	            model: '<',
	            tools: '<',
	            hideLocations: '<',
	            currentWarning: '<',
				onStructureChanged: '&',
				onZoom: '&'
	        },
	        controller: 'DiagramPaperController',
	        controllerAs: '$ctrl',
	        bindToController: true,
	        link: function (scope, element, attrs) {
	        	var $ctrl = scope.$ctrl,
	        		$paperEl = element.find('.joint-paper'),
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
	                highlighting: {
	                	'default': {
	                		name: 'addClass',
	                		options: {
	                			className: 'highlighted'
	                		}
	                	}
	                },
	                interactive: {
	                	vertexAdd: false,
	                	labelMove: true
	                }
	            });
	        	
	        	// MIN_ZOOM should not be changed to a greater value to ensure all integer percentages from 1 to 100 are valid
	        	var MIN_ZOOM = 0.01,		
	        		MAX_ZOOM = 500;
	        	
	        	// Setup pan and zoom functionality
	        	// See http://plnkr.co/edit/djYRygTGnQOvaBICk1dE?p=preview
	        	var paperPanZoom = svgPanZoom(paper.svg, {
					viewportSelector: paper.viewport,
					fit: false,
					contain: false,
					center: false,
					zoomScaleSensitivity: 0.05,
					dblClickZoomEnabled: false,
					panEnabled: false,
					beforePan: beforePan,
					minZoom: MIN_ZOOM,
					maxZoom: MAX_ZOOM,
					// http://ariutta.github.io/svg-pan-zoom/demo/mobile.html
					customEventsHandler: {
						haltEventListeners: [/*'touchstart', 'touchend',*/ 'touchmove'/*, 'touchleave', 'touchcancel'*/],
			        	init: function (options) {
			        		var instance = options.instance,
			        			initialScale = 1,
			        			pannedX,
			        			pannedY,
				        		pinchCenter;
			        		
			        		function panBy(dx, dy) {
			        			if (instance.isPanEnabled()) {
			        				instance.panBy({ x: dx - pannedX, y: dy - pannedY });
			        				pannedX = dx;
			        				pannedY = dy;
			        			}
			        		}
			        		
			        		// Init Hammer
			        		// Listen only for pointer and touch events
			        		this.hammer = new Hammer.Manager(options.svgElement, {
			        			inputClass: Hammer.SUPPORT_POINTER_EVENTS ? Hammer.PointerEventInput : Hammer.TouchInput
			        		});
			        		this.hammer.add(new Hammer.Pinch());
			        		this.hammer.add(new Hammer.Pan({ direction: Hammer.DIRECTION_ALL }));
			        		this.hammer.add(new Hammer.Tap({ event: 'doubletap', taps: 2 }));
			        		// Handle pinch
			        		this.hammer.on('pinchstart', function (ev) {
			        			initialScale = instance.getZoom();
			        			pinchCenter = ev.center;
			        		});
			        		this.hammer.on('pinchmove', function (ev) {
			        			instance.zoomAtPoint(initialScale * ev.scale, pinchCenter);
			        		});
			        		
			        		this.hammer.on('panstart', function (ev) {
			        			pannedX = 0;
			        			pannedY = 0;
			        			panBy(ev.deltaX, ev.deltaY);
			        		});
			        		this.hammer.on('panmove', function (ev) {
			        			panBy(ev.deltaX, ev.deltaY);
			        		});
			        		this.hammer.on('doubletap', function (ev) {
			        			paper.mousedblclick($.Event(ev.srcEvent));
			        		});
			        		// Prevent moving the page on some devices when panning over SVG
			        		options.svgElement.addEventListener('touchmove', function (e) {
			        			e.preventDefault();
			        		});
			        	},
			        	destroy: function() {
			        		this.hammer.destroy();
			        	}
					}
				});
				// Update paper dimensions on resize
	        	$window.addEventListener('resize', onResize);
				//Enable pan when a blank area is click (held) on
				paper.on('blank:pointerdown', enablePan);
				//Disable pan when the mouse button is released
				paper.on('cell:pointerup blank:pointerup', disablePan);
				
				scope.$on('render:done', initialPanAndZoom);
				scope.$on('diagram:change', updateBBox);
				model.on('add remove', $ctrl.wrapInTimeout(updateBBox));
				
				
				function onResize() {
					paper.setDimensions($paperEl.width(), $paperEl.height());
					paperPanZoom.resize();
				}
				
				// http://ariutta.github.io/svg-pan-zoom/demo/limit-pan.html
				function beforePan(oldPan, newPan) {
					var sizes = paperPanZoom.getSizes(),
					
						leftLimit = -((sizes.viewBox.x + sizes.viewBox.width) * sizes.realZoom) + GUTTER_WIDTH,
			            rightLimit = sizes.width - GUTTER_WIDTH - (sizes.viewBox.x * sizes.realZoom),
			            topLimit = -((sizes.viewBox.y + sizes.viewBox.height) * sizes.realZoom) + GUTTER_WIDTH,
			            bottomLimit = sizes.height - GUTTER_WIDTH - (sizes.viewBox.y * sizes.realZoom),
					
		          		xOK = _.inRange(newPan.x, leftLimit, rightLimit),
		          		yOK = _.inRange(newPan.y, topLimit, bottomLimit);
		          		
		          return xOK === yOK ? xOK : { x: xOK, y: yOK };
				}
				
				function initialPanAndZoom() {
					updateBBox();
					// Temporarily limit zoom to 100% to prevent extreme values in case the diagram is (almost) empty.
					paperPanZoom.setMaxZoom(1);
					fitAndCenter();
					paperPanZoom.setMaxZoom(MAX_ZOOM);
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
				
				$ctrl.paperPanZoom = paperPanZoom;				
				$ctrl.onPaperInit(paper);
	        } 
	    };
	}
	
	return diagramPaperDirective;
	
});