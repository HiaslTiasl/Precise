define([
	'lib/lodash',
	'lib/joint',
	'shapes/BaseShape',
	'shapes/TemplateUtil',
	'util/util'
], function (
	_,
	joint,
	BaseShape,
	TemplateUtil,
	util
) {
	
	var LOC_COL_WIDTH = 20,
		LOC_ROW_HEIGHT = LOC_COL_WIDTH,
		DEFAULT_LOC_HEIGHT = 1 * LOC_ROW_HEIGHT;
	
	var WIDTH = 8 * LOC_COL_WIDTH,
		HEADER_ROW_HEIGHT = LOC_ROW_HEIGHT,
		HEADER_COL_WIDTH = WIDTH / 4,
		NAME_HEIGHT = 2 * HEADER_ROW_HEIGHT,
		LOC_POS_Y = HEADER_ROW_HEIGHT + NAME_HEIGHT,
		DEFAULT_HEIGHT = LOC_POS_Y + DEFAULT_LOC_HEIGHT;
	
	var WILDCARD_VALUE = "*";
	
	var textClasses = [
		'task-id',
		'task-workers-needed',
		'task-units-per-day',
		'task-type-craft',
		'task-type-name',
		'task-order',
		'task-exclusiveness'
	];
	
	var rectClasses = ['outline', 'task-locations'].concat(textClasses);
	
	var TaskShape = util.set(joint.shapes, ['precise', 'TaskShape'], BaseShape.extend(/*_.extend({}, joint.plugins.precise.TaskToolsShape,*/ {
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<g class="task-locations"/>',
					TemplateUtil.createElements('rect', rectClasses).join(''),
				'</g>',
				TemplateUtil.createElements('text', textClasses).join(''),
			'</g>',
		].join(''),
		
		defaults: joint.util.deepSupplement({
			type: 'precise.TaskShape',
			size: {
				width: WIDTH,
				height: DEFAULT_HEIGHT,
			},
			cusWidth: 0,
			attrs: _.assign({
				rect: {
					width: WIDTH,
					'follow-scale': true
				},
				'rect.task-id, rect.task-workers-needed, rect.task-units-per-day, rect.task-type-craft': {
					width: HEADER_COL_WIDTH,
					height: HEADER_ROW_HEIGHT
				},
				'rect.task-id':             { x: 0 * HEADER_COL_WIDTH },
				'rect.task-workers-needed': { x: 1 * HEADER_COL_WIDTH },
				'rect.task-units-per-day':  { x: 2 * HEADER_COL_WIDTH },
				'rect.task-type-craft':     { x: 3 * HEADER_COL_WIDTH },
				
				'rect.task-type-name': { y: HEADER_ROW_HEIGHT, height: NAME_HEIGHT },
				'rect.task-locations': { y: LOC_POS_Y,  height: DEFAULT_LOC_HEIGHT },
				'rect.loc-entry': {
					width: LOC_COL_WIDTH,
					height: LOC_ROW_HEIGHT,
					'stroke-width': 1,
					'follow-scale': true
				}
			}, TemplateUtil.withRefsToSameClass('text', 'rect', textClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, BaseShape.prototype.defaults),
		
		initialize: function (options) {
			this.set('id', TaskShape.toTaskID(options.data.id));
			BaseShape.prototype.initialize.apply(this, arguments);
		},
		
		update: function () {
			var data = this.get('data'),
				attributes = data.type.phase.attributes,
				attrCount = attributes.length,
				exclusive = data.globalExclusiveness || data.exclusiveness.length,
				locationsHeight = attrCount * LOC_ROW_HEIGHT,
				locationPatterns = data.locationPatterns,
				width = WIDTH,
				height = LOC_POS_Y + locationsHeight;
			if (exclusive) {
				width += 10;
				height += 10;
			}
			
			this.set('size', { width: width, height: height });
			this.set('position', data.position);
			this.attr({
				'rect.outline': {
					width: width,
					height: height,
					transform: exclusive ? 'translate(-5,-5)' : ''
				},
				'rect.task-locations':      { height: locationsHeight },
				'text.task-id':             { text: data.id },
				'text.task-workers-needed': { text: data.numberOfWorkersNeeded },
				'text.task-units-per-day':  { text: data.numberOfUnitsPerDay },
				'text.task-type-craft':     { text: data.type.craftShort },
				'text.task-type-name':      { text: joint.util.breakText(data.type.name, { width: WIDTH, height: NAME_HEIGHT }) }
			});
			if (locationPatterns) {
				for (var i = 0, locLen = locationPatterns.length; i < locLen; i++) {
					var pattern = locationPatterns[i];
					for (var j = 0; j < attrCount; j++) {
						var attrName = attributes[j].name,
							value = pattern[attrName].value,
							rectSelector = 'rect.loc-entry.loc-num-' + i + '.' + attrName,
							textSelector = 'text.loc-entry.loc-num-' + i + '.' + attrName;
						this.attr(rectSelector, {
							x: i * LOC_COL_WIDTH,
							y: j * LOC_ROW_HEIGHT + LOC_POS_Y
						});
						this.attr(textSelector, {
							'ref-x': .5,
							'ref-y': .5,
							'text-anchor': 'middle',
							'y-alignment': 'middle',
							'ref': rectSelector,
							'text': value
						});
						
					}
				}
			}
		}
		
	}, {
		// Static properties
		WIDTH: WIDTH,
		NAME_POS_Y: HEADER_ROW_HEIGHT,
		NAME_HEIGHT: NAME_HEIGHT,
		LOC_POS_Y: LOC_POS_Y,
		DEFAULT_HEIGHT: DEFAULT_HEIGHT,
		
		toTaskID: function (id) {
			return 'task-' + id;
		}
	}));
	
	// http://stackoverflow.com/a/30275325
	util.set(joint.shapes, 'precise.TaskShapeView', joint.dia.ElementView.extend({
		
		locRectsTemplate: [
			'<% _.forEach(attributes, function (attr) { %>',
				'<rect class="loc-num-${num} loc-entry ${attr.name}"/>',
			'<% }); %>'
		].join(''),
		
		locTextsTemplate: [
   			'<% _.forEach(attributes, function (attr) { %>',
   				'<text class="loc-num-${num} loc-entry ${attr.name}">',
   					'${pattern[attr.name].value}',
				'</text>',
   			'<% }); %>'
   		].join(''),
   		
   	    initialize: function() {

   	        _.bindAll(this, 'renderLocations');

   	        this.positionChangeBatchOptions = { batchName: 'position-change', other: { cell: this.model }};
   	        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

   	        this.listenTo(this.model, 'change:data', this.renderLocations);
   	    },
   	    
   	    render: function () {
   	    	joint.dia.ElementView.prototype.render.apply(this, arguments);
   	    	this.update();
   	    },
   		
		update: function () {
			// Update attributes for new elements
			this.renderLocations();
			joint.dia.ElementView.prototype.update.apply(this, arguments);
		},
		
		renderLocations: function () {
			var data = this.model.get('data'),
				attributes = data.type.phase.attributes,
				locationPatterns = data.locationPatterns,
				actualLocationCount = locationPatterns ? locationPatterns.length : 0,
				renderedLocationCount = this.scalableNode.find('.loc-entry').length / attributes.length;
			
			if (actualLocationCount < renderedLocationCount) {				
				// Remove shapes that have been removed
				for (var i = actualLocationCount; i < renderedLocationCount; i++) {
					this.rotatableNode.find('.loc-num-' + i).forEach(function (vShape) {
						vShape.remove();
					});
				}
			}
			else if (actualLocationCount > renderedLocationCount) {
				// Add shapes for new locations
				var rectsTemplateFn = TemplateUtil.compile(this.locRectsTemplate),
					textsTemplateFn = TemplateUtil.compile(this.locTextsTemplate),
					args = {
						attributes: attributes
					};
				for (var i = renderedLocationCount; i < actualLocationCount; i++) {
					args.pattern = locationPatterns[i];
					args.num = i;
					this.scalableNode.append(joint.V(rectsTemplateFn(args)));				
					this.rotatableNode.append(joint.V(textsTemplateFn(args)));				
				}
			}
		},
		
		pointerdown: function () {
			this.model.trigger('batch:start', this.positionChangeBatchOptions);
			joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
		},
		
		pointerup: function () {
			joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
			this.model.trigger('batch:stop', this.positionChangeBatchOptions);
		},
		
		highlight: function () {
			this.model.attr({
				'rect.outline': {
					filter: {
						name: 'dropShadow',
						args: {
							color: 'black',
							dx: 5,
							dy: 5,
							blur: 3,
							opacity: 0.7 
						}

					}
				}
//				'rect.outline': {
//					filter: {
//						name: 'outline',
//						args: {
//							color: 'red',
//							width: 1,
//							opacity: 5,
//							margin: 2 
//						}
//	
//					}
//				}
//				'rect.outline': {
//					filter: {
//						name: 'highlight',
//						args: {
//							color: 'red',
//							width: 1,
//							blur: 5,
//							opacity: 1 
//						}
//					
//					}
//				}
			});
			joint.dia.ElementView.prototype.highlight.apply(this, arguments);
		},
		
		unhighlight: function () {
			this.model.attr('rect.outline/filter', 'none');
			joint.dia.ElementView.prototype.unhighlight.apply(this, arguments);
		}
		
	}));
	
	return TaskShape;
});