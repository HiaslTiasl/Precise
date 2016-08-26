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
	
	var WIDTH = 16,
		HEIGHT = WIDTH * 4,
		ROW_HEIGHT = HEIGHT / 4,
		DEFAULT_HEIGHT = ROW_HEIGHT;
	
	Util.set(joint.shapes, 'precise.LocationShape', BaseShape.extend({
		markup: [
			'<g class="rotatable">',
				'<g class="scalable"></g>',
			'</g>'
		].join(''),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.LocationShape',
			size: {
				width: WIDTH,
				height: ROW_HEIGHT,
			},
			attrs: _.assign({
				rect: {
					width: WIDTH,
					height: ROW_HEIGHT,
					'stroke-width': 1,
					'follow-scale': true
				}
			})
		}, BaseShape.prototype.defaults),
		
		update: function () {
			var data = this.get('data'),
				pattern = data.pattern;
			
			this.set('size', {
				width: WIDTH,
				height: pattern.length * ROW_HEIGHT
			});
			pattern.forEach(function (entry, i) {
				var rectSelector = 'rect.' + entry.attribute,
					textSelector = 'text.' + entry.attribute;
				this.attr(rectSelector, {
					y: i * ROW_HEIGHT
				});
				this.attr(textSelector, {
					'ref-x': .5,
					'ref-y': .5,
					'text-anchor': 'middle',
					'y-alignment': 'middle',
					'ref': rectSelector,
					'text': entry.value
				});
			}, this);
		},
		
	}, {
		// Static properties
		WIDTH: WIDTH,
		ROW_HEIGHT: ROW_HEIGHT,
		DEFAULT_HEIGHT: DEFAULT_HEIGHT,
		
		toLocationID: function (id) {
			return 'location-' + id;
		}
	}));
	
	Util.set(joint.shapes, 'precise.LocationShapeView', joint.dia.ElementView.extend({
		
		entryRectTemplate: '<rect class="<%= attribute %>"/>',
		
		entryTextTemplate: '<text class="<%= attribute %>"><%= value %></text>',
		
		update: function () {
			// Add rect and text for each entry
			var rectTemplateFn = TemplateUtil.compile(this.entryRectTemplate),
				textTemplateFn = TemplateUtil.compile(this.entryTextTemplate);
			this.model.get('data').pattern.forEach(function (entry) {
				this.scalableNode.append(joint.V(rectTemplateFn(entry)));				
				this.rotatableNode.append(joint.V(textTemplateFn(entry)));				
			}, this);
			// Update attributes for new elements
			joint.dia.ElementView.prototype.update.apply(this, arguments);
		}
	
	}));
	
	return joint.shapes.precise.LocationShape;
});