/**
 * JointJS model and view for dependencies
 * @module "shapes/DependencyShape"
 */
define([
	'lib/lodash',
	'lib/joint',
	'api/hal',
	'util/util'
], function (
	_,
	joint,
	HAL,
	util
) {
	'use strict';
	
	var MODEL_CLASSPATH = 'precise.DependencyShape',	// Path of the class under joint.shapes and type attribute, so JointJS can find the class
		VIEW_CLASSPATH = MODEL_CLASSPATH + 'View';		// As expected by JointJS to find the view implementation of a model class
	
	var ARROW_MARKER = 'm 0 10 l -24 10 l 24 10 z',
		CHAIN_FILTER_ID = 'chain-precedence',
		ALT_CROSS_SIZE = 16;
	
	/**
	 * JointJS cell model of a dependency.
	 * @constructor
	 * @extends joint.dia.Link
	 */
	var DependencyShape = util.set(joint.shapes, MODEL_CLASSPATH, joint.dia.Link.extend({
		
		// --------------------- Custom markup -------------------------
		//
		// Changes wrt. default markup are annotated with comments.
		
		/** Basic markup. */
		markup: [
			'<path class="connection" stroke="black" d="M 0 0 0 0"/>',
			'<path class="marker-source" fill="black" stroke="black" d="M 0 0 0 0"/>',
			// An arrow marker
			'<path class="marker-target" fill="black" stroke="black" d="m 0 10 l -16 6 l 16 6 z"/>',
			// the 'X' to indicate alternate precedences
			'<path class="alt-cross" d="M 8 8 L -8 -8 M 8 -8 L -8 8"/>',
			'<path class="connection-wrap" d="M 0 0 0 0"/>',
			'<g class="labels"/>',
			'<g class="marker-vertices"/>',
			'<g class="marker-arrowheads"/>',
			'<g class="link-tools"/>'
		].join(''),
		
		/** Markup for link tools. */
		toolMarkup: [
			'<g class="link-tool">',
				'<g class="tool-remove" event="remove">',
					// Increased radius
					'<circle r="20" />',
					// changed path scale from 0.8 to 1 (implicit)
					'<path transform="translate(-16, -16)" d="M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 16.447,18.746 21.948,24.248z" />',
					'<title>Remove link.</title>',
				'</g>',
				'<g class="tool-options" event="link:options">',
					'<circle r="11" transform="translate(25)"/>',
					'<path fill="white" transform="scale(.55) translate(29, -16)" d="M31.229,17.736c0.064-0.571,0.104-1.148,0.104-1.736s-0.04-1.166-0.104-1.737l-4.377-1.557c-0.218-0.716-0.504-1.401-0.851-2.05l1.993-4.192c-0.725-0.91-1.549-1.734-2.458-2.459l-4.193,1.994c-0.647-0.347-1.334-0.632-2.049-0.849l-1.558-4.378C17.165,0.708,16.588,0.667,16,0.667s-1.166,0.041-1.737,0.105L12.707,5.15c-0.716,0.217-1.401,0.502-2.05,0.849L6.464,4.005C5.554,4.73,4.73,5.554,4.005,6.464l1.994,4.192c-0.347,0.648-0.632,1.334-0.849,2.05l-4.378,1.557C0.708,14.834,0.667,15.412,0.667,16s0.041,1.165,0.105,1.736l4.378,1.558c0.217,0.715,0.502,1.401,0.849,2.049l-1.994,4.193c0.725,0.909,1.549,1.733,2.459,2.458l4.192-1.993c0.648,0.347,1.334,0.633,2.05,0.851l1.557,4.377c0.571,0.064,1.148,0.104,1.737,0.104c0.588,0,1.165-0.04,1.736-0.104l1.558-4.377c0.715-0.218,1.399-0.504,2.049-0.851l4.193,1.993c0.909-0.725,1.733-1.549,2.458-2.458l-1.993-4.193c0.347-0.647,0.633-1.334,0.851-2.049L31.229,17.736zM16,20.871c-2.69,0-4.872-2.182-4.872-4.871c0-2.69,2.182-4.872,4.872-4.872c2.689,0,4.871,2.182,4.871,4.872C20.871,18.689,18.689,20.871,16,20.871z"/>',
					'<title>Link options.</title>',
				'</g>',
			'</g>'
		].join(''),

		/** Markup for link vertices. */
		vertexMarkup: [
			'<g class="marker-vertex-group" transform="translate(<%= x %>, <%= y %>)">',
				// Increased radius
				'<circle class="marker-vertex" idx="<%= idx %>" r="20" />',
				// Increased size of vertex remove tool.
				'<path class="marker-vertex-remove-area" idx="<%= idx %>" d="M16,5.333c-7.732,0-14,4.701-14,10.5c0,1.982,0.741,3.833,2.016,5.414L2,25.667l5.613-1.441c2.339,1.317,5.237,2.107,8.387,2.107c7.732,0,14-4.701,14-10.5C30,10.034,23.732,5.333,16,5.333z" transform="scale(2) translate(5, -33)"/>',
				'<path class="marker-vertex-remove" idx="<%= idx %>" transform="scale(1.6) translate(9.5, -37)" d="M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 16.447,18.746 21.948,24.248z">',
					'<title>Remove vertex.</title>',
				'</path>',
			'</g>'
		].join(''),

		/** Markup for arrowheads for moving endpoints. */
		arrowheadMarkup: [
			'<g class="marker-arrowhead-group marker-arrowhead-group-<%= end %>">',
				// Increased size
				'<path class="marker-arrowhead" end="<%= end %>" d="M 40 0 L 0 20 L 40 40 z" />',
			'</g>'
		].join(''),
		
		/** Overrides default properties. */
		defaults: _.defaultsDeep({
			type: MODEL_CLASSPATH 
		}, joint.dia.Link.prototype.defaults),
		
		/** Overrides model initialization. */
		initialize: function (options) {
			// Set the ID before JointJS does so
			this.set('id', HAL.hrefTo(options.data));
			
			// Update properties when the data changes
			this.on('change:data', this.update, this);
			// Toggle visibility of labels depending on the "hideLabels" property
			this.on('change:hideLabels', this.updateHideLabels, this);
			
			// Update properties based on initial data
	        this.update();
	        // Call superclass implementation
	        joint.dia.Link.prototype.initialize.apply(this, arguments);
		},
		
		/** Property "hideLabels" changed, so update visibility of labels. */
		updateHideLabels: function (model, hideLabels) {
			this.attr('.labels/display', hideLabels ? 'none' : 'inline')
		},
		
		/** Property "data" changed, so update properties and attributes. */
		update: function () {
			var data = this.get('data') || {};
			
			// Set JointJS specific properties
			this.set({
				source: data.source ? { id: HAL.resolve(HAL.hrefTo(data.source)) } : data.sourceVertex,
				target: data.target ? { id: HAL.resolve(HAL.hrefTo(data.target)) } : data.targetVertex,
				vertices: data.vertices
			});
			// Set position and text of 
			this.label(0, {
				position: _.assign({
					distance: 0.5,		// Defaults to half the distance
					offset: -20			// Defaults to 20 above dependency if from left to right
				}, data.labelPosition),
				attrs: {
					text: {
						// Short attribute names joint by comma
						text: _.chain(data.scope).get('attributes').map('shortName').join(', ').value(),
					}
				}
			});
			var attrs = {
				// Cross indicating alternate precedence, only shown if dependency is alternate, and scaled up of also chain
				'.alt-cross': {
					'visibility': data.alternate ? 'visible' : 'hidden',
					style: { 'stroke-width': data.chain ? ('4px') : undefined },
					'transform': data.chain ? 'scale(1.4)' : ''
				},
				// The arrow line, double if chain
				'.connection': {
					filter: data.chain ? 'url(#' + CHAIN_FILTER_ID + ')' : 'none',
					style: { 'stroke-width': data.chain ? '3px' : undefined }
				}
			};
			
			this.attr(attrs);
			// Also update visibility of labels
			this.updateHideLabels(this, this.get('hideLabels'));
		}
		
	}, {
		// Static properties
		// Endpoint information regarding corresponding vertex property and opposite endpoint
		endInfo: {
			source: {
				vertex: 'sourceVertex',
				opposite: 'target'
			},
			target: {
				vertex: 'targetVertex',
				opposite: 'source'
			}
		}
		
	}));
	
	// Color matrix that increases contrast
	var intensify = [
		1, 0, 0,    0, 0, 
		0, 1, 0,    0, 0,
		0, 0, 1,    0, 0,
		0, 0, 0, 1000, 0
	].join(' ');
	
	// Color matrix that maps alpha to white
	var visibleToWhite = [
		0, 0, 0, 0, 1, 
		0, 0, 0, 0, 1,
		0, 0, 0, 0, 1,
		0, 0, 0, 1, 0
	].join(' ');
	
	/**
	 * JointJS cell view for dependencies.
	 * Uses JointJS batch operations for complex operations that involve dragging
	 * (i.e. moving endpoints, vertices, or labels) to trigger events only when
	 * such operations are finished, which can be used to update the server.
	 * @constructor
	 * @extends joint.dia.LinkView
	 */
	util.set(joint.shapes, VIEW_CLASSPATH, joint.dia.LinkView.extend({ 
		/**
		 * SVG filter for producing double-stroked paths, used for chain precedences.
		 * Achieves this by scaling up the input image, and drawing it again on top but
		 * in white.
		 * The contrast is increased to make it look cleaner.
		 */
		filterMarkup: [
			'<filter id="' + CHAIN_FILTER_ID + '" filterUnits="userSpaceOnUse">',
				'<feColorMatrix in="SourceGraphic" result="inner" type="matrix" values="' + intensify + '"/>',
				'<feMorphology in="inner" result="outer" operator="dilate" radius="2"/>',
				'<feColorMatrix in="inner" result="innerWhite" type="matrix" values="' + visibleToWhite + '"/>',
				'<feMerge>',
					'<feMergeNode in="outer"/>',
					'<feMergeNode in="innerWhite"/>',
				'</feMerge>',
			'</filter>'
		].join(''),
		
		/** Maps this._action as reported by JointJS to custom batch operation names. */
		batchNameByAction: {
			'vertex-move': 'vertices-change',
			'arrowhead-move': 'end-change',
			'label-move': 'label-change'
		},
		
		/** Overrides default options. */
		options: _.defaults({
			sampleInterval: 20	// Smaller than default to fix issues regarding label positioning for short links
		}, joint.dia.LinkView.prototype.options),
		
		/** Creates a JointJS batch options object for the given name and optional other options. */
		createBatchOptions: function (name, other) {
			return {
				batchName: name,
				other: _.assign({
					cell: this.model	// Always include the cell model, unless overridden by other
				}, other)
			};
		},
		
		/** Removes the vertex of the given index in a batch operation. */
		removeVertex: function (idx) {
			var opt = this.createBatchOptions('vertices-change');
			this.model.trigger('batch:start', opt);
			joint.dia.LinkView.prototype.removeVertex.call(this, idx);
			this.model.trigger('batch:stop', opt);
		},
		
		/** Adds the given vertex in a batch operation. */
		addVertex: function (vertex) {
			var opt = this.createBatchOptions('vertices-change');
			this.model.trigger('batch:start', opt);
        	joint.dia.LinkView.prototype.addVertex.call(this, vertex);
        	this.model.trigger('batch:stop', opt);
		},
		
		/**
		 * The pointer was put down on the dependency or a tool of it, so start the batch
		 * operation corresponding to that tool, if any.
		 */
		pointerdown: function () {
			joint.dia.LinkView.prototype.pointerdown.apply(this, arguments);
			var batchName = this.batchNameByAction[this._action];
			
			if (this.ongoingBatch)	// Should never be the case
				this.ongoingBatch = null;
			else if (batchName) {
				var opt = this.createBatchOptions(batchName, {
					end: this._arrowhead	// Set to name of endpoint for 'arrowhead-move' operation 
				});
				// Remember that this operation is ongoing
				this.ongoingBatch = opt;
				this.model.trigger('batch:start', opt);
			}
		},
		
		/**
		 * The pointer released from the dependency or a tool of it, so stop the
		 * currently ongoing batch operation, if any.
		 */
		pointerup: function () {
			joint.dia.LinkView.prototype.pointerup.apply(this, arguments);
			if (this.ongoingBatch) {
				this.model.trigger('batch:stop', this.ongoingBatch);
				this.ongoingBatch = null;
			}
		},
		
		/**
		 * There was a double-click on the dependency, so add a vertex if the
		 * click was on the arrow line.
		 */
		pointerdblclick: function (evt, x, y) {
			var vTarget = joint.V(evt.target);
            if (vTarget.hasClass('connection') || vTarget.hasClass('connection-wrap'))
            	this.addVertex({ x: x, y: y });
        },
        
        /** The model changed, so update the view. */
        update: function () {
        	// Call super to update SVG attributes
        	joint.dia.LinkView.prototype.update.apply(this, arguments);
        	this.updateAltCrossPosition();
        	this.updateFilter();
        	return this;
        },
        
        /** Updates position and orientation of the cross of the alternate precedence. */
        updateAltCrossPosition: function () {
        	// Only do something for alternate precedences, otherwise the cross is not visible anyway.
        	if (this.model.get('data').alternate) {
        		// See joint.dia.LinkView.prototype.updateLabelPositions.
	        	var connectionElement = this._V.connection.node,
	            	connectionLength = this.getConnectionLength(),
	            	startCoords = this.getPointAtLength(0),
	            	crossCoords = this.getPointAtLength(20),
	            	angle = 90 - joint.g.point(startCoords).theta(crossCoords),
	            	prevTransform = joint.V.decomposeMatrix(this._V.altCross.transform());
	        	
	        	this._V.altCross.attr('transform', '')
		        	.rotate(angle)
		        	.translate(crossCoords.x, crossCoords.y)
		        	.scale(prevTransform.scaleX, prevTransform.scaleY);
        	}
        },
        
        /**
         * Updates the SVG filter.
         * An SVG filter has a viewbox to which it applies, but in our case we cannot
         * statically know the dimensions of such a viewbox.
         * Therefore, we update the viewbox dynamically whenever updating a dependency.
         */
        updateFilter: function () {
        	// Only do something for alternate precedences, otherwise the cross is not visible anyway.
        	if (this.model.get('data').chain) {
        		var vDefs = joint.V(this.paper.defs),
        			vFilter = vDefs.findOne('#' + CHAIN_FILTER_ID),
        			bbox = this.paper.viewport.getBBox();
        		// Append the filter definition to the SVG if missing
        		// Note that this might be necessary more than once because the SVG is recreated whenever
        		// the user opens a diagram.
        		if (!vFilter) {
        			vFilter = joint.V(this.filterMarkup);
        			vDefs.append(vFilter);
        		}
        		// Set the filter viewbox to the diagram dimensions
        		vFilter.attr({
        			x: bbox.x,
        			y: bbox.y,
        			width: bbox.width,
        			height: bbox.height
        		});
        	}
        }
        
	}, {
		
		/** Returns vertices that can be used to ensure that a loop on the given taskView is visible. */
		computeLoopVertices: function (taskView, vertices, opt) {
			var selector = joint.dia.LinkView.makeSelector(taskView.model),
				element = taskView.el.querySelector(selector),
				bbox = taskView.getStrokeBBox(element),
				// Dirty hack!
				// ===========
				// We use the JointJS 'orthogonal' router to compute an orthogonal route for us.
				// JointJS routers compute additional link vertices that are only displayed but not stored,
				// so they can change dynamically, e.g. for avoiding obstacles.
				// They expect a link view, but we want to send the computed vertices to the server and
				// only then create the link and therefore the link view.
				// Therefore, we create a mock link link view that only contains the properties
				// required by this specific router, namely the bounding boxes of the source
				// and the target task.
				dummyLinkView = {
					sourceBBox: bbox,
					targetBBox: bbox
				};
			return joint.routers.orthogonal(vertices, opt || {}, dummyLinkView);
		}
	
	}));
	
	return DependencyShape;
	
});