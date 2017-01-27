/**
 * Angular directive for the DiagramPaper, wrapping it in a reusable
 * component that separates angular from the BackboneJS specific world
 * of JointJS.
 * @module "diagramPaper/diagramPaper.directive"
 */
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
	
	// Minimum portion of the diagram that must stay within the viewport when moving the paper, both vertically and horizontically
	var MIN_VISIBLE = 100;
	
	diagramPaperDirective.$inject = ['$window', '$timeout'];
	
	/** Returns the directive definition. */
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
				onDiagramChanged: '&',
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
	                	vertexAdd: false,	// Disable default vertex adding behavior on-click to allow selecting a link without adding vertices
	                	labelMove: true		// Enable moving labels
	                }
	            });
	        	
	        	// MIN_ZOOM should not be changed to a greater value to ensure all integer percentages from 1 to 100 are valid.
	        	// Otherwise during typing, an temporarily invalid value  will be changed to a valid one automatically,
	        	// which is very inconvenient.
	        	var MIN_ZOOM = 0.01,		
	        		MAX_ZOOM = 500;
	        	
	        	// Setup pan and zoom functionality
	        	// See http://plnkr.co/edit/djYRygTGnQOvaBICk1dE?p=preview
	        	// TODO: Consider the following link if a thumbnail-viewer is desired:
	        	// http://ariutta.github.io/svg-pan-zoom/demo/thumbnailViewer.html
	        	var paperPanZoom = svgPanZoom(paper.svg, {
					viewportSelector: paper.viewport,
					fit: false,
					contain: false,
					center: false,
					zoomScaleSensitivity: 0.05,
					dblClickZoomEnabled: false,
					panEnabled: false,				// Manually handle panning using HammerJS
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
			        		
			        		/** Pan by the given relative coordinates if enabled. */
			        		function panBy(dx, dy) {
			        			if (instance.isPanEnabled()) {
			        				instance.panBy({ x: dx - pannedX, y: dy - pannedY });
			        				// Update the panned coordinates for avoiding that multiple calls add up
			        				pannedX = dx;
			        				pannedY = dy;
			        			}
			        		}
			        		
			        		// Init Hammer
			        		// Listen only for pointer and touch events
			        		this.hammer = new Hammer.Manager(options.svgElement, {
			        			inputClass: Hammer.SUPPORT_POINTER_EVENTS ? Hammer.PointerEventInput : Hammer.TouchInput
			        		});
			        		// Add recognizers
			        		this.hammer.add(new Hammer.Pinch());
			        		this.hammer.add(new Hammer.Pan({ direction: Hammer.DIRECTION_ALL }));
			        		this.hammer.add(new Hammer.Tap({ event: 'doubletap', taps: 2 }));
			        		// Handle pinch
			        		this.hammer.on('pinchstart', function (ev) {
			        			// Remember initial scale and center coordinate
			        			initialScale = instance.getZoom();
			        			pinchCenter = ev.center;
			        		});
			        		this.hammer.on('pinchmove', function (ev) {
			        			// Zoom by the given scale at the initial center
			        			instance.zoomAtPoint(initialScale * ev.scale, pinchCenter);
			        		});
			        		// Handle pan
			        		this.hammer.on('panstart', function (ev) {
			        			// Reset panned coordinates
			        			pannedX = 0;
			        			pannedY = 0;
			        			panBy(ev.deltaX, ev.deltaY);
			        		});
			        		this.hammer.on('panmove', function (ev) {
			        			panBy(ev.deltaX, ev.deltaY);
			        		});
			        		this.hammer.on('doubletap', function (ev) {
			        			// Trigger a double mouse click on the paper on double tap
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
				// Enable pan when a blank area is click (held) on
				paper.on('blank:pointerdown', enablePan);
				// Disable pan when the mouse button is released
				paper.on('cell:pointerup blank:pointerup', disablePan);
				
				scope.$on('render:done', initialPanAndZoom);
				scope.$on('diagram:change', updateBBox);
				model.on('add remove', $ctrl.wrapInTimeout(updateBBox));
				
				/** The window was resized, so update the paper dimensions. */
				function onResize() {
					paper.setDimensions($paperEl.width(), $paperEl.height());
					paperPanZoom.resize();
				}
				
				/**
				 * The user attempts to change panning, so determine whether and how much that is allowed.
				 * We restrict this by requiring that the visible part of the diagram is at least MIN_VISIBLE,
				 * both vertically and horizontally.
				 * Returns a boolean to indicate whether the request pan is fine, or an object with booleans
				 * for each dimensions to indicate whether the pan in the corresponding pan is fine.
				 * See http://ariutta.github.io/svg-pan-zoom/demo/limit-pan.html
				 */
				function beforePan(oldPan, newPan) {
					var sizes = paperPanZoom.getSizes(),
					
						// The limit of each side indicates that the distance to the farthest visible point must be at least MIN_VISIBLE 
						leftLimit = -((sizes.viewBox.x + sizes.viewBox.width) * sizes.realZoom) + MIN_VISIBLE,
			            rightLimit = sizes.width - MIN_VISIBLE - (sizes.viewBox.x * sizes.realZoom),
			            topLimit = -((sizes.viewBox.y + sizes.viewBox.height) * sizes.realZoom) + MIN_VISIBLE,
			            bottomLimit = sizes.height - MIN_VISIBLE - (sizes.viewBox.y * sizes.realZoom),
					
		          		xOK = _.inRange(newPan.x, leftLimit, rightLimit),
		          		yOK = _.inRange(newPan.y, topLimit, bottomLimit);
		          		
		          return xOK === yOK ? xOK : { x: xOK, y: yOK };
				}
				
				/** Rendering of the initial graph completed, so fit the diagram on the screen. */
				function initialPanAndZoom() {
					updateBBox();
					// Temporarily limit zoom to 100% to prevent extreme values in case the diagram is (almost) empty.
					paperPanZoom.setMaxZoom(1);
					fitAndCenter();
					paperPanZoom.setMaxZoom(MAX_ZOOM);
				}
				
				/** Enables panning. */
				function enablePan() {
					paperPanZoom.enablePan();
				}
				
				/** Disables panning. */
				function disablePan() {
					paperPanZoom.disablePan();
				}
				
				/** Updates the bounding box of the diagram. */
				function updateBBox() {
					paperPanZoom.updateBBox();
				}
				
				/** Fit and center the diagram on the paper. */
				function fitAndCenter() {
					paperPanZoom.fit();
					paperPanZoom.center();
				}
				
				// Notify the controller that the paper was initialized
				// TODO: consider using the $postLink() hook in the controller instead
				$ctrl.paperPanZoom = paperPanZoom;				
				$ctrl.onPaperInit(paper);
	        }
	    };
	}
	
	return diagramPaperDirective;
	
});