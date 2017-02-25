/**
 * JointJS model and view for tasks.
 * @module "shapes/TaskShape"
 */
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
	'use strict';
	
	var MODEL_CLASSPATH = 'precise.TaskShape',			// Path of the class under joint.shapes unless and type attribute, so JointJS can find the class
		VIEW_CLASSPATH = MODEL_CLASSPATH + 'View';		// As expected by JointJS to find the view implementation of a model class
	
	// Base dimension settings
	var COL_WIDTH  = 20,	// Minimum column width
		ROW_HEIGHT = 20,	// Minimum row height
		COLS       = 8;		// Maximum number of columns of minimal width,
	
	// Derived dimensions
	var WIDTH                = COLS * COL_WIDTH,						// Total task width
		HEADER_ROW_HEIGHT    = ROW_HEIGHT,								// Height of header row (i.e. ID, crew params, duration, craft)
		HEADER_COL_WIDTH     = 2 * COL_WIDTH,							// Width of columns in header row
		ACTVITY_POS_Y        = HEADER_ROW_HEIGHT,						// Y-coordinate of top of activity field
		ACTVITY_HEIGHT       = 2.5 * ROW_HEIGHT,						// Height of activity field
		CONSTR_ROW_POS_Y     = ACTVITY_POS_Y + ACTVITY_HEIGHT,			// Y-coordinate of top of constraints row (i.e. ordering and exclusiveness)
		CONSTR_ROW_HEIGHT    = ROW_HEIGHT,								// Height of constraints row
		CONSTR_COL_WIDTH     = 4 * COL_WIDTH,							// Total width of fields in constraint row (symbol + value) 
		LOC_POS_Y            = CONSTR_ROW_POS_Y + CONSTR_ROW_HEIGHT,	// Y-coordinate of top of locations table
		LOC_ROW_HEIGHT       = ROW_HEIGHT,								// Height of rows in location table
		LOC_COL_WIDTH        = COL_WIDTH,								// Width of columns in location table
		MAX_LOC_COL_COUNT    = COLS,									// Maximum visible columns in location table
		DEFAULT_HEIGHT       = LOC_POS_Y;								// Default total task height if height of location is not known yet
	
	// Minimum space between name and border
	var NAME_PADDING = {
		x: 5,
		y: 1
	};
	
	// CSS classes for shapes that have both a <rect> and a <text> element
	var sharedClasses = [
		'task-id',						// ID field
		'task-crew',					// crew count and size field
		'task-duration',				// duration field
		'task-craft',					// craft field
		'task-activity'					// activity field
	];
	
	// Classes that display numbers from 1 to 4 above columns in the header row for being
	// able to show correspondence to any HTML input fields, currently not used.
	// TODO: Remove
	var indexClasses = ['task-id-index', 'task-workers-index', 'task-units-index', 'task-craft-index'];
	
	// CSS classes for <text> elements
	var textClasses = sharedClasses.concat([
		'task-order-symbol',			// symbol part of ordering field
		'task-order-separator',			// symbol part of ordering field
		'task-order-value',				// value part of ordering field
		'task-exclusiveness-symbol',	// symbol part of exclusiveness field
		'task-exclusiveness-separator',	// symbol part of exclusiveness field
		'task-exclusiveness-value',		// value part of exclusiveness field
	]);
	
	// CSS classes for <rect> elements,
	// i.e. classes for <text> + outline of the task + outline of the location table + outline of constraints fields
	var rectClasses = ['outline', 'task-locations', 'task-order', 'task-exclusiveness'].concat(sharedClasses);
	
	/**
	 * JointJS cell model for tasks.
	 * @constructor
	 * @extends joint.shapes.basic.Generic
	 */
	var TaskShape = util.set(joint.shapes, MODEL_CLASSPATH, joint.shapes.basic.Generic.extend({
		/** Basic markup. */
		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<g class="task-locations"/>',
					TemplateUtil.createElements('rect', rectClasses).join(''),
				'</g>',
				TemplateUtil.createElements('text', textClasses).join(''),
			'</g>',
		].join(''),
		
		/** Overrides default properties. */
		defaults: _.defaultsDeep({
			type: MODEL_CLASSPATH,
			size: {
				width: WIDTH,
				height: DEFAULT_HEIGHT,
			},
			cusWidth: 0,
			attrs: _.defaultsDeep({
				rect: {
					width: WIDTH,
					'follow-scale': true
				},
				// Header row fields
				'rect.task-id, rect.task-crew, rect.task-duration, rect.task-craft': {
					width: HEADER_COL_WIDTH,
					height: HEADER_ROW_HEIGHT
				},
				'rect.task-id':       { x: 0 * HEADER_COL_WIDTH },
				'rect.task-crew':     { x: 1 * HEADER_COL_WIDTH },
				'rect.task-duration': { x: 2 * HEADER_COL_WIDTH },
				'rect.task-craft':    { x: 3 * HEADER_COL_WIDTH },
				// Constraints fields
				'rect.task-order, rect.task-exclusiveness': {
					y: CONSTR_ROW_POS_Y,
					width: CONSTR_COL_WIDTH,
					height: CONSTR_ROW_HEIGHT	
				},
				'rect.task-exclusiveness': {
					x: CONSTR_COL_WIDTH
				},
				'text.task-order-symbol, text.task-order-separator, text.task-order-value, text.task-exclusiveness-symbol, text.task-exclusiveness-separator, text.task-exclusiveness-value': {
					y: CONSTR_ROW_POS_Y + 0.5 * ROW_HEIGHT,
					'text-anchor': 'middle',
					'dominant-baseline': 'middle'
				},
				'text.task-order-symbol':            { x: 0.5 * COL_WIDTH, text: '<' },
				'text.task-order-separator':         { x:   1 * COL_WIDTH, text: ':' },
				'text.task-order-value':             { x: 2.5 * COL_WIDTH },
				'text.task-exclusiveness-symbol':    { x: 4.5 * COL_WIDTH, text: '\u25A3' },
				'text.task-exclusiveness-separator': { x:   5 * COL_WIDTH, text: ':' },
				'text.task-exclusiveness-value':     { x: 6.5 * COL_WIDTH },
				// activity and locations
				'rect.task-activity':  { y: HEADER_ROW_HEIGHT, height: ACTVITY_HEIGHT },
				'rect.task-locations': { y: LOC_POS_Y,  height: 0, display: 'none' },
				'rect.loc-entry': {
					width: LOC_COL_WIDTH,
					height: LOC_ROW_HEIGHT
				},
				'text.trunc': {
					x: (MAX_LOC_COL_COUNT - 0.5) * LOC_COL_WIDTH,
					'text-anchor': 'middle',
					//'y-alignment': 'middle'
				}
			}, 
			// For all fields, put the <text> at the center of the <rect>
			TemplateUtil.withRefsToSameClass('text', 'rect', sharedClasses, {
				 'ref-y': .5,
				 'ref-x': .5,
				 'text-anchor': 'middle',
				 'y-alignment': 'middle'
			}))
		}, joint.shapes.basic.Generic.prototype.defaults),
		
		/** Overrides model initalization. */
		initialize: function (options) {
			// Set the ID before JointJS does so
			this.set('id', HAL.hrefTo(options.data));
			// Extract number of attributes from data to fix location table height if possible
			this.updateAttrCount(options.data);
			
			// Update properties when the data changes
			this.on('change:data', this.update, this);
			// Toggle visibility of locations depending on the "hideLocations" property
			this.on('change:hideLocations', this.updateHideLocations, this);
			
			// Update properties based on initial data
			this.update();
			// Call superclass implementation
			joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
		},
		
		/**
		 * Update number of attributes and resulting location table height based on 
		 * the given data. If the attributes are not available (i.e. if there is no
		 * phase), both the count and the height are set to zero. */
		updateAttrCount: function (data) {
			this.attrCount = _.size(_.get(data, ['activity', 'phase', 'attributes']));
			this.locationsHeight = this.attrCount * LOC_ROW_HEIGHT;
		},
		
		/** Property "hideLocations" changed, so update visibility of locations. */
		updateHideLocations: function (model, hideLocations) {
			// Hide locations, ordering, and exclusiveness
			model.attr('.loc-entry, .trunc, .task-order, .task-exclusiveness', {
				display: hideLocations ? 'none' : 'inline'
			});
			// Update the name field to use the gained space
			model.updateActivity();
		},
		
		/** The data or the available space changed, so update the activity field. */
		updateActivity: function () {
			var hideLocations = this.get('hideLocations'),
				nameHeight = hideLocations ? this.locationsHeight + CONSTR_ROW_HEIGHT + ACTVITY_HEIGHT : ACTVITY_HEIGHT,
				fontSize = hideLocations ? '150%' : '100%',		// Increase font size if more space is available due to hidden locations
				nameStyle = { 'font-size': fontSize },
				activity = this.get('data').activity,
				nameText = activity.shortName + ' - ' + activity.name;
			this.attr({
				'rect.task-activity': { height: nameHeight },
				'text.task-activity': {
					style: nameStyle,
					// break the text into multiple lines if necessary
					text: joint.util.breakText(nameText, {
						width: WIDTH - 2 * NAME_PADDING.x,
						height: nameHeight - 2 * NAME_PADDING.y
					}, {
						'style': 'font-size:' + fontSize
					})
				}
			});
		},
		
		/** Property "data" changed, so update properties and attributes. */
		update: function () {
			var data = this.get('data');
			this.updateAttrCount(data);
			var activity = data.activity,
				phase = activity.phase,
				attributes = phase && phase.attributes,
				exclusiveness = data.exclusiveness,
				exclusive = exclusiveness.type !== 'UNIT',	// UNIT is default, but an activity should always be available
				orderSpecs = data.orderSpecifications,
				locationPatterns = data.locationPatterns,
				width = WIDTH,
				height = LOC_POS_Y + this.locationsHeight,	// Total task height depends on location table (i.e. on the number of attributes)
				pitch = data.pitch,
				crewLabel = isNaN(pitch.crewSize) || isNaN(pitch.crewCount) ? '' : pitch.crewCount + '\u00d7' + pitch.crewSize,	// count times size
				durationLabel = isNaN(pitch.durationDays) ? '' : pitch.durationDays + 'd',
				craftLabel = _.get(activity, ['craft', 'shortName'], '');

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
				'rect.task-activity':            { fill: phase ? colors.toCSS(phase.color) : '#fff' },
				'rect.task-locations':           { height: this.locationsHeight, display: this.locationsHeight > 0 ? 'inline' : 'none' },
				'text.task-id':                  { text: '#' + data.id },
				'text.task-crew':                { text: crewLabel },
				'text.task-duration':            { text: durationLabel },
				'text.task-craft':               { text: craftLabel },
				'text.task-order-value':         { text: this.orderingLabel(orderSpecs) },
				'text.task-exclusiveness-value': { text: this.exclusivenessLabel(exclusiveness) }
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
			// N.B. Attributes must be set before size, otherwise the two mismatch
			this.attr(attrs);
			this.updateActivity();
			this.set({
				'position': data.position,
				'size': { width: width, height: height }
			});
		},
		
		exclusivenessLabel: function (exclusiveness) {
			return exclusiveness.type !== 'ATTRIBUTES'
				? exclusiveness.type
				: _.chain(exclusiveness).get('attributes').map('shortName').join(',').value();
		},
		
		orderingLabel: function (orderSpecs) {
			var res = _.chain(orderSpecs)
				.filter(function (os) {
					// Only show attributes that matter
					return os.orderType !== 'NONE';
				}).map(function (os) {
					// Show short attribute names and symbols indicating ordering type
					var label = os.attribute.shortName;
					switch (os.orderType) {
					case 'PARALLEL'  : return '|' + label + '|';		// e.g. |sr|
					case 'ASCENDING' : return       label + '\u2191';	// e.g.  sr↑
					case 'DESCENDING': return       label + '\u2193';	// e.g.  sr↓
					}
				}).join(',').value();
			
			return res || 'NONE';
		}
		
	}, {
		// Static properties
		WIDTH: WIDTH,
		NAME_POS_Y: HEADER_ROW_HEIGHT,
		NAME_HEIGHT: ACTVITY_HEIGHT,
		LOC_POS_Y: LOC_POS_Y,
		DEFAULT_HEIGHT: DEFAULT_HEIGHT		
	}));
	
	/**
	 * JointJS cell view for tasks.
	 * Uses JointJS batch operations for dragging to trigger events only when
	 * such an operation is finished, which can be used to update the server.
	 * @constructor
	 * @extends joint.dia.ElementView
	 */
	util.set(joint.shapes, VIEW_CLASSPATH, joint.dia.ElementView.extend({
		
		/** Template function for a column of <rect> elements for the given attributes, pattern, and num. */
		locRectsTemplate: _.template([
			'<% _.forEach(attributes, function (attr) { %>',
				'<rect class="loc-num-${num} loc-entry ${attr.name}"/>',
			'<% }); %>'
		].join('')),
		
		/** Template function for a column of <text> elements for the given attributes, pattern, and num. */
		locTextsTemplate: _.template([
   			'<% _.forEach(attributes, function (attr) { %>',
   				'<text class="loc-num-${num} loc-entry ${attr.name}">',
   					'${pattern[attr.name].value}',
				'</text>',
   			'<% }); %>'
   		].join('')),
   		
   		/** Markup for the column that indicates that there are more locations which have been truncated. */
   		truncMarkup: '<text class="trunc">...</text>',
   		
   		/**
   		 * Creates a toolbox overlay for tasks.
   		 * TODO: This is currently not used but could be extended with a remove button
   		 * (like the one for dependencies) and a button for creating dependencies.
   		 * TODO: For that, this should probably be moved into a new class which
   		 * has access to the current task.
   		 */
   		createTools: function () {
   			// http://stackoverflow.com/a/30275325
   			var tools = joint.V('<g class="task-tools"/>'),
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
   		
   		/** Updates the position of the toolbox according to that of the task. */
   		updateToolsPosition: function (model, value, options) {
   			this.tools
   				.attr('transform', '')
   				.translate(value.x, value.y);
   		},
   		
   		/**
   		 * Attach the toolbox to this task view.
   		 * Creates the toolbox if it does not exist, moves it to this task view,
   		 * and keeps the two together.
   		 */
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
		
		/**
		 * Detach the toolbox from this task view.
		 * Hides the toolbox and stops updating its position.
		 */
		detachTools: function () {
			this.model.off('change:position', this.updateToolsPosition);
			this.tools.attr('display', 'none');
			this.tools = null;
		},
   		
		/** Overrides view initialization. */
   	    initialize: function() {
   	    	
   	    	_.bindAll(this, 'updateToolsPosition');

   	        joint.dia.ElementView.prototype.initialize.apply(this, arguments);
   	        
   	 		 // N.B. don't mess update parameters up
   	        this.listenTo(this.model, 'change:data', _.ary(this.update, 0));
   	    },
   	    
   	 	/** Creates a JointJS batch options object for a position change operation. */
   	 	createPositionChangeBatchOptions: function () {
   	 		return {
   	 			batchName: 'position-change',
   	 			other: { cell: this.model }
   	 		};
   	 	},
   	    
   	 	/** (Re)-create the SVG elements. */
   	    render: function () {
   	    	joint.dia.ElementView.prototype.render.apply(this, arguments);
   	    	this.update();
   	    },
   	    
   	 	/** The model changed, so update the view. */
		update: function () {
			this.renderLocations();
			joint.dia.ElementView.prototype.update.apply(this, arguments);
		},
		
		/**
		 * Locations changed, so update the location table.
		 * Only adds or removes elements; updating the text is handled by update().
		 */
		renderLocations: function () {
			var data = this.model.get('data'),
				attributes = _.get(data, ['activity', 'phase', 'attributes']),
				locationPatterns = data.locationPatterns,
				actualLocationCount = locationPatterns ? locationPatterns.length : 0,					// Number of locations in data
				truncateLocations = actualLocationCount > MAX_LOC_COL_COUNT,							// Must truncate some locations?
				targetRenderCount = truncateLocations ? MAX_LOC_COL_COUNT - 1 : actualLocationCount, 	// Number of locations to be shown
				currentLocationCount = this.renderedLocCount || 0,										// Locations currently shown
				truncNode = this.truncNode;																// Element indicating that some locations have been truncated
			
			// N.B: we only do the minimum necessary such that in the end,
			// targetRenderCount locations, based on the currentLocationCount.
			// That is, we only add/remove the delta.
			
			if (targetRenderCount < currentLocationCount) {
				// We currently display more locations than necessary
				// -> Remove the extra ones
				for (var i = targetRenderCount; i < currentLocationCount; i++) {
					this.rotatableNode.find('.loc-num-' + i).forEach(function (vShape) {
						vShape.remove();
					});
				}
			}
			else if (targetRenderCount > currentLocationCount) {
				// We currently display less locations than necessary
				// -> Add the missing ones
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
				// We did not have truncated nodes before but we do now
				// -> create and add the truncation node
				this.truncNode = joint.V(this.truncMarkup);
				this.rotatableNode.append(this.truncNode);
			}
			else if (!truncateLocations && this.truncNode) {
				// We had truncated nodes but now we do not have any
				// -> remove the truncation node
				this.truncNode.remove();
				this.truncNode = null;
			}
			// Remember the number of rendered location for future delta updates
			this.renderedLocCount = targetRenderCount;
			
		},
		
		/**
		 * The pointer was put down on the task, so start a batch operation
		 * for moving the task.
		 */
		pointerdown: function () {
			this.model.trigger('batch:start', this.createPositionChangeBatchOptions());
			joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
		},
		
		/**
		 * The pointer was released from the task, so stop a batch operation
		 * for moving the task.
		 */
		pointerup: function () {
			joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
			this.model.trigger('batch:stop', this.createPositionChangeBatchOptions());
		},
		
		/** Highlight this task. */
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
			// Call super implementation
			joint.dia.ElementView.prototype.highlight.apply(this, arguments);
		},
		
		/** Unhighlight this task. */
		unhighlight: function () {
			this.model.attr('rect.outline/filter', 'none');
			//this.detachTools();
			// Call super implementation
			joint.dia.ElementView.prototype.unhighlight.apply(this, arguments);
		}
		
	}));
	
	return TaskShape;
});