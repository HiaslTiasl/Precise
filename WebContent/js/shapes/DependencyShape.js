define([
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'util/util'
], function (
	_,
	joint,
	TaskShape,
	util
) {
	'use strict';
	
	var ARROW_MARKER = 'm 0 10 l -24 10 l 24 10 z',
		CHAIN_FILTER_ID = 'chain-precedence',
		ALT_CROSS_SIZE = 16;
	
	var getScopeLabel = _.property('shortName');
	
	var DependencyShape = util.set(joint.shapes, ['precise', 'DependencyShape'], joint.dia.Link.extend({
		
		// --------------------- Custom markup -------------------------
		//
		// Changes wrt. default markup are annotated with comments.
		
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

		arrowheadMarkup: [
			'<g class="marker-arrowhead-group marker-arrowhead-group-<%= end %>">',
				// Increased size
				'<path class="marker-arrowhead" end="<%= end %>" d="M 40 0 L 0 20 L 40 40 z" />',
			'</g>'
		].join(''),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.DependencyShape'
		}, joint.dia.Link.prototype.defaults),
		
		initialize: function (options) {
			this.set('id', DependencyShape.toDependencyID(options.data.id));
			
			this.on('change:data', this.update, this);
			this.on('change:hideLabels', this.updateHideLabels, this);
			
	        this.update();
	        
	        joint.dia.Cell.prototype.initialize.apply(this, arguments);
		},
		
		updateHideLabels: function (model, hideLabels) {
			this.attr('.labels/display', hideLabels ? 'none' : 'inline')
		},
		
		update: function () {
			var data = this.get('data') || {};
			
			this.set({
				source: data.sourceID ? DependencyShape.idToEndpoint(data.sourceID) : data.sourceVertex,
				target: data.targetID ? DependencyShape.idToEndpoint(data.targetID) : data.targetVertex,
				vertices: data.vertices
			});
			this.label(0, {
				position: 0.5,
				attrs: {
					text: {
						text: data.scope && data.scope.map(getScopeLabel).join(', '),
						transform: 'translate(-10, -10)'
					}
				}
			});
			var attrs = {
				'.alt-cross': {
					'visibility': data.alternate ? 'visible' : 'hidden',
					style: { 'stroke-width': data.chain ? ('4px') : undefined },
					'transform': data.chain ? 'scale(1.4)' : ''
				},
				'.connection': {
					filter: data.chain ? 'url(#' + CHAIN_FILTER_ID + ')' : 'none',
					style: { 'stroke-width': data.chain ? '3px' : undefined }
				}
			};
			
			this.attr(attrs);
			this.updateHideLabels(this, this.get('hideLabels'));
		}
		
	}, {
		// Static properties
		toDependencyID: function (id) {
			return 'dependency-' + id;
		},
		
		idToEndpoint: function (id) {
			return {
				id: TaskShape.toTaskID(id)
			};
		},
		
		endInfo: {
			source: {
				id: 'sourceID',
				vertex: 'sourceVertex',
				opposite: 'target'
			},
			target: {
				id: 'targetID',
				vertex: 'targetVertex',
				opposite: 'source'
			}
		}
		
	}));
	
	var intensify = [
		1, 0, 0,    0, 0, 
		0, 1, 0,    0, 0,
		0, 0, 1,    0, 0,
		0, 0, 0, 1000, 0
	].join(' ');
	
	var visibleToWhite = [
		0, 0, 0, 0, 1, 
		0, 0, 0, 0, 1,
		0, 0, 0, 0, 1,
		0, 0, 0, 1, 0
	].join(' ');
	
	util.set(joint.shapes, 'precise.DependencyShapeView', joint.dia.LinkView.extend({
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
		
		batchNameByAction: {
			'vertex-move': 'vertices-change',
			'arrowhead-move': 'end-change'
		},
		
		initialize: function () {
			this.batchOptions = _.transform(this.batchNameByAction, function (res, batchName) {
				res[batchName] = {
					batchName: batchName,
					other: { cell: this.model }
				};
			}, {}, this);
			joint.dia.LinkView.prototype.initialize.apply(this, arguments);
		},
		
		removeVertex: function (idx) {
			var opt = this.batchOptions['vertices-change'];
			this.model.trigger('batch:start', opt);
			joint.dia.LinkView.prototype.removeVertex.call(this, idx);
			this.model.trigger('batch:stop', opt);
		},
		
		addVertex: function (vertex) {
			var opt = this.batchOptions['vertices-change'];
			this.model.trigger('batch:start', opt);
        	joint.dia.LinkView.prototype.addVertex.call(this, vertex);
        	this.model.trigger('batch:stop', opt);
		},
		
		pointerdown: function () {
			joint.dia.LinkView.prototype.pointerdown.apply(this, arguments);
			var batchName = this.batchNameByAction[this._action];
			if (!this.ongoingBatch && batchName) {
				var opt = this.batchOptions[batchName];
				if (batchName === 'end-change')
					opt.other.end = this._arrowhead;
				this.ongoingBatch = opt;
				this.model.trigger('batch:start', opt);
			}
		},
		
		pointerup: function () {
			joint.dia.LinkView.prototype.pointerup.apply(this, arguments);
			if (this.ongoingBatch) {
				this.model.trigger('batch:stop', this.ongoingBatch);
				this.ongoingBatch = null;
			}
		},
		
		pointerdblclick: function (evt, x, y) {
            if (joint.V(evt.target).hasClass('connection') || joint.V(evt.target).hasClass('connection-wrap'))
            	this.addVertex({ x: x, y: y });
        },
        
        update: function () {
        	joint.dia.LinkView.prototype.update.apply(this, arguments);
        	this.updateAltCrossPosition();
        	this.updateFilter();
        	return this;
        },
        
        updateAltCrossPosition: function () {
        	// See joint.dia.LinkView.prototype.updateLabelPositions.
        	var connectionElement = this._V.connection.node,
            	connectionLength = this.getConnectionLength(),
            	startCoords = this.getPointAtLength(0),
            	coords = this.getPointAtLength(20),
            	angle = 90 - joint.g.point(startCoords).theta(coords),
            	prevTransform = joint.V.decomposeMatrix(this._V.altCross.transform());
        	
        	this._V.altCross.attr('transform', '')
	        	.rotate(angle)
	        	.translate(coords.x, coords.y)
	        	.scale(prevTransform.scaleX, prevTransform.scaleY);
        },
        
        updateFilter: function () {
        	if (this.model.get('data').chain) {
        		var vDefs = joint.V(this.paper.defs),
        			vFilter = vDefs.findOne('#' + CHAIN_FILTER_ID),
        			bbox = this.paper.viewport.getBBox();
        		if (!vFilter) {
        			vFilter = joint.V(this.filterMarkup);
        			vDefs.append(vFilter);
        		}
        		vFilter.attr({
        			x: bbox.x,
        			y: bbox.y,
        			width: bbox.width,
        			height: bbox.height
        		});
        	}
        }
        
	}, {
		
		computeLoopVertices: function (taskView, vertices, opt) {
			var selector = joint.dia.LinkView.makeSelector(taskView.model),
				element = taskView.el.querySelector(selector),
				bbox = taskView.getStrokeBBox(element);
			return joint.routers.orthogonal(vertices, opt || {}, {
				sourceBBox: bbox,
				targetBBox: bbox
			});
		}
	
	}));
	
	return DependencyShape;
	
});