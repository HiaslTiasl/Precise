define([
	'lib/lodash',
	'lib/joint',
	'shapes/TemplateUtil',
	'api/colors',
	'api/hal',
	'util/util'
], function (
	_,
	joint,
	TemplateUtil,
	colors,
	HAL,
	util
) {
	
	var COL_WIDTH  = 20,
		ROW_HEIGHT = 20,
		COLS       = 8;
	
	var WIDTH             = COLS * COL_WIDTH,
		HEADER_ROW_HEIGHT = ROW_HEIGHT,
		HEADER_COL_WIDTH  = 2 * COL_WIDTH,
		NAME_POS_Y        = HEADER_ROW_HEIGHT,
		NAME_HEIGHT       = 2.5 * ROW_HEIGHT,
		CONSTR_ROW_POS_Y  = NAME_POS_Y + NAME_HEIGHT,
		CONSTR_ROW_HEIGHT = ROW_HEIGHT,
		CONSTR_COL_WIDTH  = 4 * COL_WIDTH,
		LOC_POS_Y         = CONSTR_ROW_POS_Y + CONSTR_ROW_HEIGHT,
		LOC_COL_WIDTH     = COL_WIDTH,
		LOC_ROW_HEIGHT    = ROW_HEIGHT,
		MAX_LOC_COL_COUNT = COLS
		DEFAULT_HEIGHT    = LOC_POS_Y;
	
	var NAME_PADDING = {
		x: 5,
		y: 1
	};
	
	var WILDCARD_VALUE = "*";
	
	var sharedClasses = [
		'task-id',
		'task-crew',
		'task-duration',
		'task-order',
		'task-exclusiveness',
		'task-type-craft',
		'task-type-name'
	];
	
	var indexClasses = ['task-id-index', 'task-workers-index', 'task-units-index', 'task-craft-index'];
	
	var textClasses = sharedClasses.slice()
	
	var rectClasses = ['outline', 'task-locations'].concat(sharedClasses);
	
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
				'rect.task-id, rect.task-crew, rect.task-duration, rect.task-type-craft': {
					width: HEADER_COL_WIDTH,
					height: HEADER_ROW_HEIGHT
				},
				'rect.task-id':            { x: 0 * HEADER_COL_WIDTH },
				'rect.task-crew':          { x: 1 * HEADER_COL_WIDTH },
				'rect.task-duration':      { x: 2 * HEADER_COL_WIDTH },
				'rect.task-type-craft':    { x: 3 * HEADER_COL_WIDTH },
				
				'rect.task-order, rect.task-exclusiveness': {
					y: CONSTR_ROW_POS_Y,
					width: CONSTR_COL_WIDTH,
					height: CONSTR_ROW_HEIGHT
				},
				'rect.task-order':         { x: 0 * CONSTR_COL_WIDTH },
				'rect.task-exclusiveness': { x: 1 * CONSTR_COL_WIDTH },
				
				'rect.task-type-name':     { y: HEADER_ROW_HEIGHT, height: NAME_HEIGHT },
				'rect.task-locations':     { y: LOC_POS_Y,  height: 0, display: 'none' },
				'rect.loc-entry': {
					width: LOC_COL_WIDTH,
					height: LOC_ROW_HEIGHT
				},
				'text.trunc': {
					x: (MAX_LOC_COL_COUNT - 0.5) * LOC_COL_WIDTH,
					'text-anchor': 'middle',
					//'y-alignment': 'middle'
				}
			}, TemplateUtil.withRefsToSameClass('text', 'rect', sharedClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, joint.shapes.basic.Generic.prototype.defaults),
		
		initialize: function (options) {
			this.updateAttrCount(options.data);

			this.set('id', HAL.hrefTo(options.data));
			
			this.on('change:data', this.update, this);
			this.on('change:hideLocations', this.updateHideLocations, this);
			
			this.update();
			
			joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
		},
		
		updateAttrCount: function (data) {
			this.attrCount = _.size(_.get(data, ['type', 'phase', 'attributes']));
			this.locationsHeight = this.attrCount * LOC_ROW_HEIGHT;
		},
		
		updateHideLocations: function (model, hideLocations) {
			model.attr('.loc-entry, .trunc, .task-order, .task-exclusiveness', {
				display: hideLocations ? 'none' : 'inline'
			});
			model.updateName();
		},
		
		updateName: function () {
			var hideLocations = this.get('hideLocations'),
				nameHeight = hideLocations ? this.locationsHeight + CONSTR_ROW_HEIGHT + NAME_HEIGHT : NAME_HEIGHT,
				fontSize = hideLocations ? '150%' : '100%',
				nameStyle = { 'font-size': fontSize },
				type = this.get('data').type,
				nameText = type.shortName + ' - ' + type.name;
			this.attr({
				'rect.task-type-name': { height: nameHeight },
				'text.task-type-name': {
					style: nameStyle,
					text: joint.util.breakText(nameText, {
						width: WIDTH - 2 * NAME_PADDING.x,
						height: nameHeight - 2 * NAME_PADDING.y
					}, {
						'style': 'font-size:' + fontSize
					})
				}
			});
		},
		
		update: function () {
			var data = this.get('data');
			this.updateAttrCount(data);
			var type = data.type,
				phase = type.phase,
				attributes = phase && phase.attributes,
				exclusiveness = data.exclusiveness,
				orderSpecifications = data.orderSpecifications,
				exclusive = exclusiveness.type !== 'UNIT',
				locationPatterns = data.locationPatterns,
				width = WIDTH,
				height = LOC_POS_Y + this.locationsHeight,
				pitch = data.pitch,
				crew = isNaN(pitch.crewSize) || isNaN(pitch.crewCount) ? '' : pitch.crewCount + '\u00d7' + pitch.crewSize,
				duration = isNaN(pitch.durationDays) ? '' : pitch.durationDays + 'd',
				craft = _.get(data, ['type', 'craft', 'shortName'], '');

			if (exclusive) {
				width += 10;
				height += 10;
			}
			
			var attrs = {
				'rect.outline': {
					width: width,
					height: height,
					transform: exclusive ? 'translate(-5,-5)' : ''
				},
				'rect.task-type-name':     { fill: phase ? colors.toCSS(phase.color) : '#fff' },
				'rect.task-locations':     { height: this.locationsHeight, display: this.locationsHeight > 0 ? 'inline' : 'none' },
				'text.task-id':            { text: '#' + data.id },
				'text.task-crew':          { text: crew },
				'text.task-duration':      { text: duration },
				'text.task-type-craft':    { text: craft },
				'text.task-exclusiveness': {
					text: 'ex: (' + _.chain(exclusiveness).get('attributes').map('shortName').join(',').value() + ')'
				},
				'text.task-order': {
					text: 'or: (' + _.chain(orderSpecifications).filter(function (os) {
						return os.orderType !== 'NONE';
					}).map(function (os) {
						var label = os.attribute.shortName;
						switch (os.orderType) {
						case 'PARALLEL'  : return '|' + label + '|';
						case 'ASCENDING' : return       label + '\u2191';
						case 'DESCENDING': return       label + '\u2193';
						}
					}).join(',').value() + ')' },
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
			}
			this.attr(attrs);
			this.updateName();
			this.set({
				'position': data.position,
				'size': { width: width, height: height }
			});
		}
		
	}, {
		// Static properties
		WIDTH: WIDTH,
		NAME_POS_Y: HEADER_ROW_HEIGHT,
		NAME_HEIGHT: NAME_HEIGHT,
		LOC_POS_Y: LOC_POS_Y,
		DEFAULT_HEIGHT: DEFAULT_HEIGHT		
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
   		
   		createTools: function () {
   			var bbox = this.vel.bbox(),
				tools = joint.V('<g class="task-tools"/>'),
				x, y = -HEADER_ROW_HEIGHT;
			TemplateUtil.createElements('text', indexClasses).forEach(function (e, i) {
				x = (i + 0.5) * HEADER_COL_WIDTH;
				tools.append(joint.V(e)
					.text('(' + (1 + i) + ')')
					.attr({
						'text-anchor': 'middle',
						'transform': 'translate(' + x + ',' + y + ')'
					}));
   			});
			return tools;
   		},
   		
   		updateToolsPosition: function (model, value, options) {
   			this.tools
   				.attr('transform', '')
   				.translate(value.x, value.y);
   		},
   		
   		attachTools: function () {
			var vViewport = joint.V(this.paper.viewport);
			this.tools = vViewport.findOne('.task-tools');
			if (!this.tools) {
				this.tools = this.createTools();
				vViewport.append(this.tools);
			}
			this.model.on('change:position', this.updateToolsPosition);
			this.updateToolsPosition(this.model, this.model.get('position'));
			this.tools.attr('display', 'inline');
		},
		
		detachTools: function () {
			this.model.off('change:position', this.updateToolsPosition);
			this.tools.attr('display', 'none');
			this.tools = null;
		},
   		
   	    initialize: function() {
   	    	
   	    	_.bindAll(this, 'updateToolsPosition');

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
				attributes = _.get(data, ['type', 'phase', 'attributes']),
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
			//this.attachTools();
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
			});
			joint.dia.ElementView.prototype.highlight.apply(this, arguments);
		},
		
		unhighlight: function () {
			this.model.attr('rect.outline/filter', 'none');
			//this.detachTools();
			joint.dia.ElementView.prototype.unhighlight.apply(this, arguments);
		}
		
	}));
	
	return TaskShape;
});