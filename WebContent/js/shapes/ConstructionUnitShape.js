define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseShape',
	'shapes/TemplateUtil',
	'Util'
], function (
	_,
	joint,
	BaseShape,
	TemplateUtil,
	Util
) {
	
	var WIDTH = 25,
		HEIGHT = 100,
		ROW_HEIGHT = HEIGHT / 4;
	
	var classes = [
		'cu-sector',
		'cu-level',
		'cu-section',
		'cu-unit'
	];
	
	Util.set(joint.shapes, 'precise.ConstructionUnitShape', BaseShape.extend({
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					TemplateUtil.createElements('rect', classes).join(''),
				'</g>',
				TemplateUtil.createElements('text', classes).join(''),
			'</g>'
		].join(''),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.ConstructionUnitShape',
			size: {
				width: WIDTH,
				height: HEIGHT,
			},
			attrs: _.assign({
				rect: {
					width: WIDTH,
					height: ROW_HEIGHT,
					'stroke-width': 1,
					'follow-scale': true
				},
				'rect.cu-level':   { y: 1 * ROW_HEIGHT },
				'rect.cu-section': { y: 2 * ROW_HEIGHT },
				'rect.cu-unit':    { y: 3 * ROW_HEIGHT }
			}, TemplateUtil.withRefsToSameClass('text', 'rect', classes, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, BaseShape.prototype.defaults),
		
		update: function () {
			var data = this.get('data') || {};
			this.attr({
				'text.cu-sector':  { text: data.sector },
				'text.cu-level':   { text: data.level },
				'text.cu-section': { text: data.section },
				'text.cu-unit':    { text: data.unit }
			});
		},
		
	}, {
		WIDTH: WIDTH,
		HEIGHT: HEIGHT,
		ROW_HEIGHT: ROW_HEIGHT
	}));
	
	Util.set(joint.shapes, 'precise.ConstructionUnitShapeView', joint.dia.ElementView.extend({
		pointerdown: function () {
			joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
			// TODO: wire up moving logic
			//this.model.startMoving
		}
	}));
	
	return joint.shapes.precise.ConstructionUnitShape;
});