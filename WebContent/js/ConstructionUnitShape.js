define(['lodash', 'joint', 'BaseShape', 'TaskShape', 'TemplateUtil'], function (_, joint, BaseShape, TaskShape, TemplateUtil) {
	
	var WIDTH = TaskShape.WIDTH / 8,
		HEIGHT = TaskShape.CUS_HEIGHT,
		ROW_HEIGHT = HEIGHT / 4;
	
	var classes = [
		'cu-sector',
		'cu-level',
		'cu-section',
		'cu-unit'
	];
	
	return BaseShape.extend({
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
					'follow-scale': true
				},
				'rect.cu-level':   { y: 1/4 * HEIGHT },
				'rect.cu-section': { y: 2/4 * HEIGHT },
				'rect.cu-unit':    { y: 3/4 * HEIGHT }
			}, TemplateUtil.withRefsToSameClass('text', 'rect', classes, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, BaseShape.prototype.defaults),
		
		update: function () {
			var data = this.get('data');
			this.attr({
				'text.cu-sector':  { text: data.sector },
				'text.cu-level':   { text: data.level },
				'text.cu-section': { text: data.section },
				'text.cu-unit':    { text: data.unit }
			});
		},
		
		isAllowedAction: function (name) {
			switch (name) {
			case 'cell:pointermove':
				return false;
			default:
				return true;
			}
	    },
		
	}, {
		WIDTH: WIDTH,
		HEIGHT: HEIGHT,
		ROW_HEIGHT: ROW_HEIGHT
	});
});