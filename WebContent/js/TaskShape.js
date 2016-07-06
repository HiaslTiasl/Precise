define(['lodash', 'joint', 'BaseShape', 'TemplateUtil'], function (_, joint, BaseShape, TemplateUtil) {
	
	var WIDTH = 400,
		HEIGHT = WIDTH,
		ROW_HEIGHT = WIDTH / 4,
		CUS_POS_Y = 2 * ROW_HEIGHT,
		CUS_HEIGHT = HEIGHT - CUS_POS_Y;
	
	var textClasses = [
		'task-id',
		'task-workers',
		'task-time-units',
		'task-craft',
		'task-name',
	];
	
	var classes = textClasses.concat('task-cus');
	
	return BaseShape.extend({
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					TemplateUtil.createElements('rect', classes).join(''),
				'</g>',
				TemplateUtil.createElements('text', textClasses).join(''),
			'</g>'
		].join(''),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.TaskShape',
			size: {
				width: WIDTH,
				height: HEIGHT,
			},
			cusWidth: 0,
			attrs: _.assign({
				rect: {
					width: WIDTH,
					height: ROW_HEIGHT,
					'follow-scale': true
				},
				'rect.task-id, rect.task-workers, rect.task-time-units, rect.task-craft': {
					width: WIDTH / 4
				},
				'rect.task-workers':    { x: 1/4 * WIDTH },
				'rect.task-time-units': { x: 2/4 * WIDTH },
				'rect.task-craft':      { x: 3/4 * WIDTH },
				
				'rect.task-name': { y:     ROW_HEIGHT },
				'rect.task-cus':  { y: 2 * ROW_HEIGHT, height: CUS_HEIGHT },
			}, TemplateUtil.withRefsToSameClass('text', 'rect', textClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, BaseShape.prototype.defaults),
		
		update: function () {
			var data = this.get('data');
			this.attr({
				'text.task-id':         { text: data.id },
				'text.task-workers':    { text: data.workers },
				'text.task-time-units': { text: data.timeUnits },
				'text.task-craft':      { text: data.craft },
				'text.task-name':       { text: data.name }
			});
		},
		
		embed: function (cell) {
			BaseShape.prototype.embed.call(this, cell);
			var cusWidth = this.get('cusWidth');
			cell.position(cusWidth, CUS_POS_Y, {
				parentRelative: true
			});
			this.set('cusWidth', cusWidth + cell.get('size').width);
		},
		
		unembed: function (cell) {
			this.cusWidth -= cell.get('size').width;
			BaseShape.prototype.unembed.call(this, cell);
		}
	}, {
		WIDTH: WIDTH,
		HEIGHT: HEIGHT,
		ROW_HEIGHT: ROW_HEIGHT,
		CUS_POS_Y: CUS_POS_Y,
		CUS_HEIGHT: CUS_HEIGHT
	});
});