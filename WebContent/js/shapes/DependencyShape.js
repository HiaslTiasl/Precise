define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseMixin',
	'util/util'
], function (
	_,
	joint,
	BaseMixin,
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
	
	util.set(joint.shapes, ['precise', 'DependencyShape'], joint.dia.Link.extend(BaseMixin).extend({
		
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
		
		update: function () {
			var data = this.get('data') || {},
				connectionStroke = data.chain ? 2 : 1;
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
		}
	}, {
		// Static properties
		scopeLabels: scopeLabels,
		
		toDependencyID: function (id) {
			return 'dependency-' + id;
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
		
		render: function () {
			joint.dia.LinkView.prototype.render.apply(this, arguments);
			if (this.model.get('data').chain)
				defineFilter(this.paper, this.filterMarkup);
		},
		
		pointerdblclick: function (evt, x, y) {
            if (joint.V(evt.target).hasClass('connection') || joint.V(evt.target).hasClass('connection-wrap')) {
                this.addVertex({ x: x, y: y });
            }
        },
        
        update: function () {
        	joint.dia.LinkView.prototype.update.apply(this, arguments);
        	this.updateAltCrossPosition();
        	return this;
        },
        
        updateAltCrossPosition: function () {
        	// See joint.dia.LinkView.prototype.updateLabelPositions.
        	var connectionElement = this._V.connection.node,
            	connectionLength = connectionElement.getTotalLength(),
            	startCoords = connectionElement.getPointAtLength(0),
            	crossCoords = connectionElement.getPointAtLength(20),
            	theta = joint.g.point(startCoords).theta(crossCoords);
        		
        	this._V.altCross
        		.attr('transform', '')
        		.rotate(90 - theta)
        		.translate(crossCoords.x, crossCoords.y);
        }
        
	}));
	
	return joint.shapes.precise.DependencyShape;
	
});