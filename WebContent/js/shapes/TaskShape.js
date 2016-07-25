define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseShape',
	'shapes/ConstructionUnitShape',
	'shapes/TemplateUtil',
	'Util'//,
	//'TaskToolsShape'
], function (
	_,
	joint,
	BaseShape,
	ConstructionUnitShape,
	TemplateUtil,
	Util
) {
	
	var WIDTH = 200,
		NAME_POS_Y = WIDTH / 8,
		NAME_HEIGHT = WIDTH / 4,
		CUS_POS_Y = NAME_POS_Y + NAME_HEIGHT,
		CUS_HEIGHT = ConstructionUnitShape.HEIGHT,
		HEIGHT = CUS_POS_Y + CUS_HEIGHT;
	
	var textClasses = [
		'task-id',
		'task-workers',
		'task-time-units',
		'task-craft',
		'task-name',
	];
	
	var classes = textClasses.concat('task-cus');
	
	function compareX(cell1, cell2) {
		return cell1.get('position').x - cell2.get('position').x;
	}
	
	Util.set(joint.shapes, ['precise', 'TaskShape'], BaseShape.extend(/*_.extend({}, joint.plugins.precise.TaskToolsShape,*/ {
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<rect class="outline"/>',
					TemplateUtil.createElements('rect', classes).join(''),
				'</g>',
				TemplateUtil.createElements('text', textClasses).join(''),
			'</g>',
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
					'follow-scale': true
				},
				'rect.task-id, rect.task-workers, rect.task-time-units, rect.task-craft': {
					width: WIDTH / 4,
					height: NAME_POS_Y
				},
				'rect.task-workers':    { x: 1/4 * WIDTH },
				'rect.task-time-units': { x: 2/4 * WIDTH },
				'rect.task-craft':      { x: 3/4 * WIDTH },
				
				'rect.task-name': { y: NAME_POS_Y, height: NAME_HEIGHT },
				'rect.task-cus':  { y: CUS_POS_Y,  height: CUS_HEIGHT },
			}, TemplateUtil.withRefsToSameClass('text', 'rect', textClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, BaseShape.prototype.defaults),
		
//		initialize: function () {
//			joint.plugins.precise.TaskToolsShape.initialize.apply(this, arguments);
//			BaseShape.prototype.initialize.apply(this, arguments);
//		},
		
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
			var cusWidth = this.get('cusWidth'),
				embeds = this.get('embeds'),
				count = embeds ? embeds.length : 0;
			BaseShape.prototype.embed.call(this, cell);
			cell.position(count * ConstructionUnitShape.WIDTH, CUS_POS_Y, {
				parentRelative: true
			});
		},
		
		unembed: function (cell) {
			this.cusWidth -= cell.get('size').width;
			BaseShape.prototype.unembed.call(this, cell);
		},
		
		updateCuPositions: function (fromIndex) {
			var embeds = this.getEmbeddedCells(),
				len = embeds.length;
			for (var i = fromIndex || 0; i < len; i++)
				embeds[i].translate(-ConstructionUnitShape.WIDTH);
		},
		
		startMoveConstructionUnit: function (cu) {
			var embeds = this.getEmbeddedCells(),
				index = Util.binarySearch(embeds, 0, 0, cu, compareX);
			if (index >= 0) {
				// Found in array (should always be the case)
				this.updateCuPositions(index);
				this.movingCuIndex = index;		// Save index, do not search again on endMove
			}
		},
		
		endMoveConstructionUnit: function (cu) {
			var embeds = this.getEmbeddedCells();
			embeds.splice(this.movingCuIndex, 1);
			var index = Util.binarySearch(embeds, 0, 0, cu, compareX);
			if (index < 0) {
				// Not found in array (should always be the case)
				var insertPos = -(index + 1);
				embeds.splice(insertPos, 0, cu);
				this.updateCuPositions(insertPos);
				this.movingCuIndex = null;		// Reset index
			}
		}
	}, {
		WIDTH: WIDTH,
		HEIGHT: HEIGHT,
		NAME_POS_Y: NAME_POS_Y,
		NAME_HEIGHT: NAME_HEIGHT,
		CUS_POS_Y: CUS_POS_Y
	}));
	
	// http://stackoverflow.com/a/30275325
	Util.set(joint.shapes, 'precise.TaskShapeView', joint.dia.ElementView.extend({
		outlineMarkup: '<rect class="outline"/>',
		
		renderOutline: function () {
			var markup = this.outlineMarkup
				|| this.model.outlineMarkup
				|| this.model.get('outlineMarkup');
			
			if (markup) {
				joint.V(this.el).prepend(
					joint.V(markup).attr({
						width: WIDTH,
						height: HEIGHT
					})
					.translate(-WIDTH * 0.025, -HEIGHT * 0.025)
					.scale(1.05)
					//.translate(WIDTH / 2, HEIGHT / 2)
					
				);
			}
	        return this;
		},
		
		render: function () {
			joint.dia.ElementView.prototype.render.apply(this, arguments);
			this.renderOutline();
			this.update();
			return this;
		}
		
	}));
	
	return joint.shapes.precise.TaskShape;
});