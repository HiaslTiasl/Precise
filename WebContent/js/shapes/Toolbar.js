define(['lib/lodash', 'lib/joint', 'TemplateUtil', 'Util'], function (_, joint, TemplateUtil, Util) {
	
	var HEIGHT = 50;
	
	var classes = ['palette-task', 'palette-precedence']
	
	return Util.set(joint.shapes, ['precise', 'Toolbar'], joint.shapes.basic.Rect.extend({
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<rect class="toolbar-select"/>',
					'<rect class="toolbar-new-task"/>',
					'<rect class="toolbar-remove-task"/>',
					'<rect class="toolbar-precedence"/>',
				'</g>',
				'<text class="palette-task"/>',
				'<text class="palette-precedence"/>',
			'</g>'
		].join(''),
		
		defaults: {
			size: {
				width: WIDTH,
				height: HEIGHT
			},
			attrs: {
				'text.palette-task':       { text: 'Task' },
				'text.palette-precedence': { text: 'Precedence' },
			}
		}
	}));
});