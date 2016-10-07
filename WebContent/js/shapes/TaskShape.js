define([
	'lib/lodash',
	'lib/joint',
	'shapes/TemplateUtil',
	'api/colors',
	'util/util'
], function (
	_,
	joint,
	TemplateUtil,
	colors,
	util
) {
	
	var hideLocations = true;
	
	var LOC_COL_WIDTH = 20,
		LOC_ROW_HEIGHT = LOC_COL_WIDTH,
		DEFAULT_LOC_HEIGHT = 1 * LOC_ROW_HEIGHT,
		MAX_LOC_COL_COUNT = 8;
	
	var WIDTH = MAX_LOC_COL_COUNT * LOC_COL_WIDTH,
		HEADER_ROW_HEIGHT = LOC_ROW_HEIGHT,
		HEADER_COL_WIDTH = WIDTH / 4,
		NAME_HEIGHT = 2 * HEADER_ROW_HEIGHT,
		LOC_POS_Y = HEADER_ROW_HEIGHT + NAME_HEIGHT,
		DEFAULT_HEIGHT = LOC_POS_Y + DEFAULT_LOC_HEIGHT;
	
	var NAME_PADDING = {
		x: 10,
		y: 2
	};
	
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
	
	var TaskShape = util.set(joint.shapes, ['precise', 'TaskShape'], joint.shapes.basic.Generic.extend({
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
					height: LOC_ROW_HEIGHT
				},
				'text.trunc': {
					x: (MAX_LOC_COL_COUNT - 0.5) * LOC_COL_WIDTH,
					'text-anchor': 'middle',
					//'y-alignment': 'middle'
				}
			}, TemplateUtil.withRefsToSameClass('text', 'rect', textClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, joint.shapes.basic.Generic.prototype.defaults),
		
		initialize: function (options) {
			this.attrCount = options.data.type.phase.attributes.length;
			this.locationsHeight = this.attrCount * LOC_ROW_HEIGHT;

			this.set('id', TaskShape.toTaskID(options.data.id));
			
			this.on('change:data', this.update, this);
			this.on('change:hideLocations', this.updateHideLocations, this);

			this.update();
			
			joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
		},
		
		updateHideLocations: function (model, hideLocations) {
			var nameHeight = hideLocations ? model.locationsHeight + NAME_HEIGHT : NAME_HEIGHT,
				fontSize = hideLocations ? '150%' : '100%',
				nameStyle = { 'font-size': fontSize };
			this.attr({
				'rect.task-type-name': { height: nameHeight },
				'text.task-type-name':       {
					style: nameStyle,
					text: joint.util.breakText(model.get('data').type.name, {
						width: WIDTH - 2 * NAME_PADDING.x,
						height: nameHeight - 2 * NAME_PADDING.y
					}, {
						'style': 'font-size:' + fontSize
					})
				},
				'.loc-entry, .trunc': { display: hideLocations ? 'none' : 'inline' }
			});
			
		},
		
		update: function () {
			var data = this.get('data'),
				attributes = data.type.phase.attributes,
				exclusiveness = data.exclusiveness,
				exclusive = exclusiveness.type === 'GLOBAL'
					|| (exclusiveness.type === 'ATTRIBUTES' && _.size(exclusiveness.attributes)),
				locationPatterns = data.locationPatterns,
				width = WIDTH,
				height = LOC_POS_Y + this.locationsHeight;

			if (exclusive) {
				width += 10;
				height += 10;
			}
			
			this.set({
				'position': data.position,
				'size': { width: width, height: height }
			});
			var attrs = {
				'rect.outline': {
					width: width,
					height: height,
					transform: exclusive ? 'translate(-5,-5)' : ''
				},
				'rect.task-type-name':       { fill: colors.toCSS(data.type.phase.color) },
				'rect.task-locations':       { height: this.locationsHeight },
				'text.task-id':              { text: data.id },
				'text.task-workers-needed':  { text: data.numberOfWorkersNeeded },
				'text.task-units-per-day':   { text: data.numberOfUnitsPerDay },
				'text.task-type-craft':      { text: data.type.craftShort }
			};
			if (locationPatterns) {
				attrs['text.trunc'] = {
					y: LOC_POS_Y + this.locationsHeight / 2
				};
				var locLen = Math.min(locationPatterns.length, MAX_LOC_COL_COUNT);
				for (var i = 0; i < locLen; i++) {
					var pattern = locationPatterns[i];
					for (var j = 0; j < this.attrCount; j++) {
						var attrName = attributes[j].name,
							value = pattern[attrName].value,
							rectSelector = 'rect.loc-entry.loc-num-' + i + '.' + attrName,
							textSelector = 'text.loc-entry.loc-num-' + i + '.' + attrName;

						attrs[rectSelector] = {
							x: i * LOC_COL_WIDTH,
							y: j * LOC_ROW_HEIGHT + LOC_POS_Y
						};
						attrs[textSelector] = {
							'ref-x': .5,
							'ref-y': .5,
							'text-anchor': 'middle',
							'y-alignment': 'middle',
							'ref': rectSelector,
							'text': value
						};
						
					}
				}
				this.attr(attrs);
			}
			this.updateHideLocations(this, this.get('hideLocations'));
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
		
		locRectsTemplate: _.template([
			'<% _.forEach(attributes, function (attr) { %>',
				'<rect class="loc-num-${num} loc-entry ${attr.name}"/>',
			'<% }); %>'
		].join('')),
		
		locTextsTemplate: _.template([
   			'<% _.forEach(attributes, function (attr) { %>',
   				'<text class="loc-num-${num} loc-entry ${attr.name}">',
   					'${pattern[attr.name].value}',
				'</text>',
   			'<% }); %>'
   		].join('')),
   		
   		truncMarkup: '<text class="trunc">...</text>',
   		
   	    initialize: function() {

   	        this.positionChangeBatchOptions = { batchName: 'position-change', other: { cell: this.model }};
   	        joint.dia.ElementView.prototype.initialize.apply(this, arguments);
   	        
   	 		 // N.B. don't messup update parameters
   	        this.listenTo(this.model, 'change:data', _.ary(this.update, 0));
   	        this.listenTo(this.model, 'change:hideLocations', this.toggleHideLocations);
   	    },
   	    
   	    render: function () {
   	    	joint.dia.ElementView.prototype.render.apply(this, arguments);
   	    	this.update();
   	    },
   	    
   	    toggleHideLocations: function (hideLocations) {
   	    	this.vel.toggleClass('hide-locations', hideLocations);
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
				truncateLocations = actualLocationCount > MAX_LOC_COL_COUNT,
				targetRenderCount = truncateLocations ? MAX_LOC_COL_COUNT - 1 : actualLocationCount, 
				currentLocationCount = this.renderedLocCount || 0,
				truncNode = this.truncNode;
			
			if (targetRenderCount < currentLocationCount) {				
				// Remove shapes that have been removed
				for (var i = targetRenderCount; i < currentLocationCount; i++) {
					this.rotatableNode.find('.loc-num-' + i).forEach(function (vShape) {
						vShape.remove();
					});
				}
			}
			else if (targetRenderCount > currentLocationCount) {
				// Add shapes for new locations
				var args = {
					attributes: attributes
				};
				for (var i = currentLocationCount; i < targetRenderCount; i++) {
					args.pattern = locationPatterns[i];
					args.num = i;
					this.scalableNode.append(joint.V(this.locRectsTemplate(args)));				
					this.rotatableNode.append(joint.V(this.locTextsTemplate(args)));				
				}
			}
			if (truncateLocations && !this.truncNode) {
				this.truncNode = joint.V(this.truncMarkup);
				this.rotatableNode.append(this.truncNode);
			}
			else if (!truncateLocations && this.truncNode) {
				this.truncNode.remove();
				this.truncNode = null;
			}
			
			this.renderedLocCount = targetRenderCount;
			
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