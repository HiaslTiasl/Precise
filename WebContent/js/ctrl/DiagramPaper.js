define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape',
	'api/hal',
	'util/util'
], function (
	$,
	_,
	joint,
	TaskShape,
	DependencyShape,
	HAL,
	util
) {
	'use strict';
	
	var DependencyShapeView = joint.shapes.precise.DependencyShapeView;
	
	var NS_DIAGRAM = 'diagram',
		NS_TASK = 'task',
		NS_DEPENDENCY = 'dependency';
	
	var CLASS_WARNING = 'warning',
		CLASS_SEARCH_RESULTS = 'search-results',
		CLASS_SEARCH_RESULT = 'search-result';
	
	var nsToType = {},
		typeToNs;
	
	nsToType[NS_TASK] = TaskShape.prototype.defaults.type;
	nsToType[NS_DEPENDENCY] = DependencyShape.prototype.defaults.type;
	
	typeToNs = _.invert(nsToType);
	
	var defaultEditMode = {
		className: 'default-mode',
		listeners: {
			'blank:pointerdown': function () {
				this.unselect();
			}
		}
	};
	
	function idOfEntity(e) {
		return HAL.resolve(HAL.hrefTo(e));
	}
	
	var DiagramPaper = util.defineClass(Backbone.Events, {
		
		constructor: function DiagramPaper(paper) {
			this.paper = paper;
			//this.selectedView;
			//this.selectedNS;
			//this.editMode;
			//this.warningTasks;
			//this.warningDependencies;
			this.attachListeners();
			this.setEditMode(defaultEditMode);
		},
	
		attachListeners: function (selector) {
			// Proxy all paper events
			this.listenTo(this.paper, 'all', this.trigger);
			// Selecting cells
			this.listenTo(this.paper, 'cell:pointerup', this.onCellPointerup);
			// React on internal operations
			this.listenTo(this.paper.model, 'remove', this.onRemove)
			this.listenTo(this.paper.model, 'batch:stop', this.onBatchStop)
		},
		
		onCellPointerup: function (cellView, event, x, y) {
			this.select(cellView.model.graph && cellView);
		},
		
		onRemove: function (cell) {
			if (!cell.removedRemotely) {
				if (this.selectedView && this.selectedView.model === cell)
					this.select(null, this.selectedNS);
				this.triggerNS('remove', typeToNs[cell.get('type')], [cell.get('data')])
			}
		},
		
		onBatchStop: function (options) {
			var cell = options.other && options.other.cell;
			if (cell) {
				var data = cell.get('data'),
					changedData,
					changedNS;
				switch (options.batchName) {
				case 'vertices-change':
					changedData = this.changedVerticesData(options, cell, data);
					changedNS = NS_DEPENDENCY;
					break;
				case 'end-change':
					changedData = this.changedEndData(options, cell, data);
					changedNS = NS_DEPENDENCY;
					break;
				case 'label-change': 
					changedData = this.changedLabel(options, cell, data);
					changedNS = NS_DEPENDENCY;
					break;
				case 'position-change':
					changedData = this.changedPositionData(options, cell, data);
					changedNS = NS_TASK;
					break;
				}
				if (changedData)
					this.triggerNS('change', changedNS, [_.defaults(changedData, data)]);	
			}
		},
		
		changedVerticesData: function (options, cell, data) {
			var vertices = cell.get('vertices'),
				source = cell.get('source'),
				target = cell.get('target');
			if (!vertices.length && source.id && source.id === target.id) {
				var taskView = this.paper.findViewByModel(this.paper.model.getCell(source.id));
				vertices = DependencyShapeView.computeLoopVertices(taskView);
			}
			return vertices === data.vertices ? null : { vertices: vertices };
		},
		
		changedEndData: function (options, cell, data) {
			var changedData,
				end = options.other.end,
				endInfo = DependencyShape.endInfo[end],
				endVal = cell.get(end);
			if (endVal.id) {
				var endCell = this.paper.model.getCell(endVal.id);
				if (endVal.id !== HAL.resolve(HAL.hrefTo(data[end]))) {
					changedData = {};
					changedData[end] = endCell.get('data')
					if (endVal.id === cell.get(endInfo.opposite).id && !data.vertices) {
						var taskView = this.paper.findViewByModel(endCell);
						changedData[vertices] = DependencyShapeView.computeLoopVertices(taskView);
					}
				}
			}
			else if (endVal !== data[endInfo.vertex]) {
				changedData = {};
				changedData[end] = null;
				changedData[endInfo.vertex] = endVal;
			}
			return changedData;
		},
		
		changedLabel: function (options, cell, data) {
			var labelPosition = cell.label(0).position;
			return labelPosition === data.labelPosition ? null : { labelPosition: labelPosition };
		},
		
		changedPositionData: function (options, cell, data) {
			var position = cell.get('position');
			return position === data.position ? null : { position: position };
		},
		
		triggerNS: function (eventName, ns, args) {
			var nsEvent = ns + ':' + eventName,
				defaultEvent = NS_DIAGRAM + ':' + eventName;
			args.unshift(nsEvent);
			this.trigger.apply(this, args);
			args[0] = ns;
			args.unshift(defaultEvent);
			this.trigger.apply(this, args);
		},
		
		updateDimensions: function () {
			this.paper.setDimensions(this.paper.$el.width(), this.paper.$el.height());
		},
		
		unselect: function () {
			this.select(null, this.selectedNS);
		},
		
		select: function (newView, namespace) {
			var oldView = this.selectedView,
				ns = namespace || (newView && typeToNs[newView.model.get('type')]);
			if (newView !== oldView) {
				var oldNS = this.selectedNS;
				if (oldView)
					oldView.unhighlight();
				if (newView) 
					newView.highlight();
				this.triggerNS('select', ns, [newView, oldNS === ns ? oldView : null]);
				this.selectedNS = ns;
				this.selectedView = newView;
			}
		},
		
		updateCell: function (data) {
			var model = this.paper.model.getCell(HAL.hrefTo(data));
			if (model)
				model.set('data', data);
		},
		
		addCell: function (ns, data) {
			var args = {
					id: HAL.hrefTo(data), 
					data: data
				},
				cell = ns === NS_TASK ? new TaskShape(args) : new DependencyShape(args);
			this.paper.model.addCell(cell);
			this.select(this.paper.findViewByModel(cell), ns);
		},
		
		removeCell: function (ns, data) {
			this.resetEditMode();
			var model = this.paper.model.getCell(HAL.hrefTo(data));
			if (model) {
				model.removedRemotely = true;
				model.remove();
				if (model === this.selectedView.model)
					this.select(null, ns);
			}
		},
		
		fromJSON: function (rawGraph) {
			this.paper.model.fromJSON(rawGraph);
		},
		
		toggleHideLocations: function (hideLocations) {
			this.paper.model.getElements().forEach(function (taskCell, i) {
				taskCell.set('hideLocations', hideLocations);
			});
		},
		
		toggleHideLabels: function (hideLabels) {
			this.paper.model.getLinks().forEach(function (taskCell, i) {
				taskCell.set('hideLabels', hideLabels);
			});
		},
		
		toggleEntityClass: function (cellIDs, clazz, toggle) {
			var paper = this.paper;
			_.forEach(cellIDs, function (id) {
				paper.findViewByModel(id).vel.toggleClass(clazz, toggle);
			});
		},
		
		toggleLocationClass: function (locs, clazz, toggle) {
			var paper = this.paper;
			_.forEach(locs, function (l) {
				paper.findViewByModel(idOfEntity(l.task))
					.vel.find('.loc-num-' + l.index).forEach(function (v) {
						v.toggleClass(clazz, toggle);
					});
			});
		},
		
		resetClasses: function () {
			this.resetWarnings();
			this.resetSearchResults();
		},
		
		resetWarnings: function () {
			var paper = this.paper;
			if (this.warningEntities) {
				this.toggleEntityClass(this.warningEntities, CLASS_WARNING, false);
				this.warningEntities = null;
			}
			if (this.warningLocations) {
				this.toggleLocationClass(this.warningLocations, CLASS_WARNING, false);
				this.warningEntities = null;
			}
		},
		
		showWarning: function (warning) {
			this.resetClasses();
			var paper = this.paper,
				graph = paper.model;
			
			this.warningEntities = _.map(warning.entities, idOfEntity);
			this.toggleEntityClass(this.warningEntities, CLASS_WARNING, true);
			
			this.warningLocations = warning.locations;
			this.toggleLocationClass(this.warningLocations, CLASS_WARNING, true);
		},
		
		resetSearchResults: function () {
			this.paper.$el.toggleClass(CLASS_SEARCH_RESULTS, false);
			if (this.searchResults) {
				this.toggleEntityClass(this.searchResults, CLASS_SEARCH_RESULT, false);
				this.searchResults = null;
			}
		},
		
		showSearchResults: function (tasks) {
			var paper = this.paper;
			this.resetClasses();
			paper.$el.toggleClass(CLASS_SEARCH_RESULTS, true);
			this.searchResults = _.map(tasks, function (t) {
				return HAL.resolve(HAL.hrefTo(t));
			});
			this.toggleEntityClass(this.searchResults, CLASS_SEARCH_RESULT, true);
		},
		
		setEditMode: function (editMode) {
			if (editMode !== this.editMode) {
				if (this.editMode) {
					this.off(this.editMode.listeners, this);
					if (this.editMode.className)
						this.paper.$el.removeClass(this.editMode.className);
				}
				if (editMode) {
					this.on(editMode.listeners, this);
					if (editMode.className)
						this.paper.$el.addClass(editMode.className);
				}
				this.editMode = editMode;
			}
		},
		
		resetEditMode: function () {
			this.setEditMode(defaultEditMode);
		},
		
		toggleEditMode: function (editMode) {
			this.setEditMode(this.editMode !== editMode ? editMode : defaultEditMode);
		},
		
		invokeTool: function (toolDef) {
			if (toolDef.action)
				toolDef.action.call(this);
			if (toolDef.editMode)
				this.toggleEditMode(toolDef.editMode);
		},
		
		canInvokeTool: function (toolDef) {
			var reqSelView = toolDef.requiresSelected,
				reqSelNS = typeof reqSelView === 'string' && reqSelView,
				selView = this.selectedView,
				selNS = this.selectedNS;
			return !reqSelView || (selView && (!reqSelNS || reqSelNS === selNS));
		}
		
	});
	
	return DiagramPaper;
	
});