define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseMixin',
	'shapes/TaskShape',
	'util/util'
], function (
	_,
	joint,
	BaseMixin,
	TaskShape,
	util
) {
	'use strict';
	
	var ARROW_MARKER = 'm 0 6 l -16 6 l 16 6 z',
		CROSS_MARKER = 'M 15 15 L -15 -15 M 15 -15 L -15 15 M -25 0 L 15 0',
		//CROSS_MARKER = 'L -15 0 L 0 0 L -5 5 L 0 0 L 5 5 L 0 0 L 5 0 L 0 0 M 5 -5 L 0 0 L -5 -5 L 0 0 L 5 0',
		DOUBLE_CROSS_MARKER = 'M 5 5 L -5 -5 M 5 -5 L -5 5 M -15 0 L 5 0',
		CHAIN_FILTER_ID = 'chain-precedence';
	
	var scopeLabels = {
		'TASK': 't',
		'SECTOR': 'sr',
		'LEVEL': 'l',
		'SECTION': 'sn',
		'UNIT': 'u'
	};
	
	var getScopeName = _.property('name');
	
	var DependencyShape = util.set(joint.shapes, ['precise', 'DependencyShape'], joint.dia.Link.extend(BaseMixin).extend({
		
		markup: util.strInsertBefore(
			joint.dia.Link.prototype.markup,
			'<path class="alt-cross" d="M 8 8 L -8 -8 M 8 -8 L -8 8"/>',
			'<path class="connection-wrap"'
		),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.DependencyShape',
			attrs: {
				'.marker-target': {
					fill: 'black',
					'stroke-width': 1,
					d: ARROW_MARKER
				},
				'.alt-cross': {
					'stroke': 'black',
					'stroke-width': Math.sqrt(2)
				}
			},
		}, joint.dia.Link.prototype.defaults),
		
		initialize: function (options) {
			this.set('id', DependencyShape.toDependencyID(options.data.id));
			BaseMixin.initialize.apply(this, arguments);
		},
		
		update: function () {
			var data = this.get('data') || {},
				connectionStroke = data.chain ? 2 : 1;
			
			this.set('source', data.sourceID ? DependencyShape.idToEndpoint(data.sourceID) : data.sourceVertex);
			this.set('target', data.targetID ? DependencyShape.idToEndpoint(data.targetID) : data.targetVertex);
			this.set('vertices', data.vertices);
			this.label(0, {
				position: 0.5,
				attrs: {
					text: {
						text: data.scope && data.scope.map(getScopeName).join(','),
						transform: 'translate(-10, -10)'
					}
				}
			});
			this.attr({
				'.alt-cross, .connection': {
					filter: data.chain ? 'url(#' + CHAIN_FILTER_ID + ')' : 'none',
				},
				'.alt-cross': {
					'visibility': data.alternate ? 'visible' : 'hidden',
					'stroke-width': Math.sqrt(connectionStroke)
				},
				//'.marker-source': { d: data.alternate ? CROSS_MARKER : '' },
				'.connection': {
					'stroke-width': connectionStroke
				}
			});
		},
		
		localToRemoteEndpoints: function (data) {
			var endpoints = {};
			this.addRemoteEndpoint(endpoints, 'source', Array.prototype.unshift);
			this.addRemoteEndpoint(endpoints, 'target', Array.prototype.push);
			return endpoints;
		},
		
		addRemoteEndpoint: function (endpoints, data, end, arrFn) {
			var endVal = this.get(end);
			if (endVal) {
				if (endVal.id)
					endpoints[end] = endVal;
				else if (endVal.x || endVal.y) {
					if (!endpoints.vertices)
						endpoints.vertices = this.vertices ? this.vertices.slice() : [];
					arrFn.call(endpoints.vertices, endVal);
				}
			}
		},
		
	}, {
		// Static properties
		scopeLabels: scopeLabels,
		
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
	
	var defineFilter = _.once(function (paper, markup) {
		joint.V(paper.defs).append(joint.V(markup));
	});
	
	var intensify = [
		1, 0, 0,  0, 0, 
		0, 1, 0,  0, 0,
		0, 0, 1,  0, 0,
		0, 0, 0, 10, 0
	].join(' ');
	
	var invertColors = [
		 -1,  0,  0, 0, 1, 
		  0, -1,  0, 0, 1,
		  0,  0, -1, 0, 1,
		  0,  0,  0, 1, 0
	].join(' ');
	
	util.set(joint.shapes, 'precise.DependencyShapeView', joint.dia.LinkView.extend({
		filterMarkup: [
			'<filter id="' + CHAIN_FILTER_ID + '" filterUnits="userSpaceOnUse">',
				'<feColorMatrix in="SourceGraphic" result="inner" type="matrix" values="' + intensify + '"/>',
				'<feMorphology in="inner" result="outer" operator="dilate" radius="2"/>',
				'<feColorMatrix in="inner" result="innerInv" type="matrix" values="' + invertColors + '"/>',
				'<feMerge>',
					'<feMergeNode in="outer"/>',
					'<feMergeNode in="innerInv"/>',
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
		
		render: function () {
			joint.dia.LinkView.prototype.render.apply(this, arguments);
			if (this.model.get('data').chain)
				defineFilter(this.paper, this.filterMarkup);
		},
		
		removeVertex: function (idx) {
			var opt = this.batchOptions['vertices-change'];
			this.model.trigger('batch:start', opt);
			joint.dia.LinkView.prototype.removeVertex.call(this, idx);
			this.model.trigger('batch:stop', opt);
		},
		
		addVertex: function (vertex) {
			var opt = this.batchOptions['vertices-change']
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
        	return this;
        },
        
        updateAltCrossPosition: function () {
        	// See joint.dia.LinkView.prototype.updateLabelPositions.
        	var connectionElement = this._V.connection.node,
            	connectionLength = this.getConnectionLength(),
            	startCoords = this.getPointAtLength(0),
            	coords = this.getPointAtLength(20),
            	angle = 90 - joint.g.point(startCoords).theta(coords);
        		
        	this._V.altCross.attr('transform', '').rotate(angle).translate(coords.x, coords.y);
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