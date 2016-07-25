define([
	'lib/joint',
	'Util'
], function (
	joint,
	Util
) {
	var SIZE = 25;
	
	return Util.set(joint.shapes, ['precise', 'ToolShape'], joint.shapes.basic.Generic.extend({
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<rect/>',
					'<path class="symbol"/>',
				'</g>',
			'</g>'
		].join(''),
		
		defaults: {
			size: {
				width: SIZE,
				height: SIZE
			}
		},
		
		initialize: function () {
			this.attr({
				'.symbol': this.get('symbol')
			});
		},
	}));
	
});