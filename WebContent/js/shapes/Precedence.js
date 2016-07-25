define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseMixin',
	'Util'
], function (
	_,
	joint,
	BaseMixin,
	Util
) {
	
	var ARROW_PATH = 'm 0 6 l -16 6 l 16 6 z',
		ARROW_MARKER = ARROW_PATH,
		DOUBLE_ARROW_MARKER = ARROW_PATH + 'M -16 0' + ARROW_PATH;
		CROSS_MARKER = 'M 5 5 L -5 -5 M 5 -5 L -5 5 M -15 0 L 5 0';
	
	var scopeLabels = {
		'TASK': 't',
		'SECTOR': 'sr',
		'LEVEL': 'l',
		'SECTION': 'sn',
		'UNIT': 'u'
	};
	
	Util.set(joint.shapes, ['precise', 'Precedence'], joint.dia.Link.extend(BaseMixin).extend({
		defaults: joint.util.deepSupplement({
			type: 'precise.Precedence',
			attrs: {
				'.marker-target': {
					fill: 'black',
					d: ARROW_MARKER
				}
			},
			perpenducularLinks: true,
			router: {
				name: 'orthogonal'
			}
		}, joint.dia.Link.prototype.defaults),
		
		update: function () {
			var data = this.get('data') || {};
			this.label(0, {
				position: 0.5,
				attrs: {
					text: {
						text: scopeLabels[data.scope] || '<missing scope>',
						transform: 'translate(-10, -10)'
					}
				}
			});
			this.attr({
				'.marker-source': { d: data.kind === 'ALTERNATE_PRECEDENCE' ? CROSS_MARKER : '' },
				'.marker_target': { d: data.kind === 'CHAIN_PRECEDENCE' ? DOUBLE_ARROW_MARKER : '' }
			});
		}
	}, {
		scopeLabels: scopeLabels
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
	
	Util.set(joint.shapes, 'precise.PrecedenceView', joint.dia.LinkView.extend({
		filterMarkup: [
			'<filter id="stroke-doubler" filterUnits="userSpaceOnUse">',
				'<feColorMatrix in="SourceGraphic" result="intense" type="matrix" values="' + intensify + '"/>',
				'<feMorphology in="intense" result="inner" operator="dilate" radius="1"/>',
//				'<feOffset in="inner" dx="-2" dy="-2" result="outerNW"/>',
//				'<feOffset in="inner" dx="2" dy="-2" result="outerNE"/>',
//				'<feOffset in="inner" dx="-2" dy="2" result="outerSW"/>',
//				'<feOffset in="inner" dx="2" dy="2" result="outerSE"/>',
				'<feMorphology in="inner" result="outer" operator="dilate" radius="2"/>',
				'<feColorMatrix in="inner" result="innerInv" type="matrix" values="' + invertColors + '"/>',
				'<feMerge>',
//					'<feMergeNode in="outerNW"/>',
//					'<feMergeNode in="outerNE"/>',
//					'<feMergeNode in="outerSW"/>',
//					'<feMergeNode in="outerSE"/>',
					'<feMergeNode in="outer"/>',
					'<feMergeNode in="innerInv"/>',
				'</feMerge>',
			'</filter>'
		].join(''),
		
		render: function () {
			joint.dia.LinkView.prototype.render.apply(this, arguments);
			defineFilter(this.paper, this.filterMarkup);
			this.model.attr(
				'.connection/filter',
				this.model.get('data').kind === 'CHAIN_PRECEDENCE' ? 'url(#stroke-doubler)' : 'none'
			);
		}
	}));
	
	return joint.shapes.precise.Precedence;
	
});