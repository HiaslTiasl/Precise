define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape',
	'util/util'
], function (
	$,
	_,
	joint,
	TaskShape,
	DependencyShape,
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
			this.paper.on('cell:pointerup', this.onCellPointerup, this);
			// React on internal operations
			this.paper.model
				.on('remove', this.onRemove, this)
				.on('batch:stop', this.onBatchStop, this);
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
				var endCell = this.paper.model.getCell(endVal.id),
					endData = endCell.get('data');
				if (endData.id !== data[endInfo.id]) {
					changedData = {};
					changedData[end] = endData
					if (endVal.id === cell.get(endInfo.opposite).id && !data.vertices) {
						var taskView = this.paper.findViewByModel(endCell);
						changedData[vertices] = DependencyShapeView.computeLoopVertices(taskView);
					}
				}
			}
			else if (endVal !== data[endInfo.vertex]) {
				changedData = {};
				changedData[end] = null;
				changedData[endInfo.vertex] = endVal
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
		
		updateSelected: function (data) {
			this.selectedView.model.set('data', data);
		},
		
		getModelID: function (ns, data) {
			return ns === NS_TASK
				? TaskShape.toTaskID(data.id)
				: DependencyShape.toDependencyID(data.id);
		},
		
		addCell: function (ns, data) {
			var args = {
					id: this.getModelID(ns, data), 
					data: data
				},
				cell = ns === NS_TASK ? new TaskShape(args) : new DependencyShape(args);
			this.paper.model.addCell(cell);
			this.select(this.paper.findViewByModel(cell), ns);
		},
		
		removeCell: function (ns, data) {
			this.resetEditMode();
			var model = this.paper.model.getCell(this.getModelID(ns, data));
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
		
		toggleClass: function (cellIDs, clazz, toggle) {
			var paper = this.paper;
			cellIDs.forEach(function (id) {
				paper.findViewByModel(id).vel.toggleClass(clazz, toggle);
			});
		},
		
		resetClasses: function () {
			this.resetWarnings();
			this.resetSearchResults();
		},
		
		resetWarnings: function () {
			var paper = this.paper;
			if (this.warningTasks) {
				this.toggleClass(this.warningTasks, CLASS_WARNING, false);
				this.warningTasks = null;
			}
			if (this.warningDependencies) {
				this.toggleClass(this.warningDependencies, CLASS_WARNING, false);
				this.warningDependencies = null;
			}
		},
		
		showWarningForTasks: function (tasks, withDependencies) {
			this.resetClasses();
			var paper = this.paper,
				graph = paper.model;
			this.warningTasks = tasks.map(function (t) {
				var id = TaskShape.toTaskID(t.id),
					cellView = paper.findViewByModel(id);
				cellView.vel.toggleClass(CLASS_WARNING, true);
				return id;
			});
			if (withDependencies) {
				var dependencies = [];
				this.warningTasks.forEach(function (id) {
					graph.getConnectedLinks(graph.getCell(id), {
						outbound: true,
						inbound: false
					}).forEach(function (d) {
						var target = d.get('target');
						if (target.id) {
							var targetView = paper.findViewByModel(target.id);
							if (targetView.vel.hasClass(CLASS_WARNING)) {
								paper.findViewByModel(d).vel.toggleClass(CLASS_WARNING, true);
								dependencies.push(target.id);
							}
						}
					});
				})
				this.warningDependencies = dependencies;
			}
		},
		
		resetSearchResults: function () {
			this.paper.$el.toggleClass(CLASS_SEARCH_RESULTS, false);
			if (this.searchResults) {
				this.toggleClass(this.searchResults, CLASS_SEARCH_RESULT, false);
				this.searchResults = null;
			}
		},
		
		showSearchResults: function (tasks) {
			this.resetClasses();
			this.paper.$el.toggleClass(CLASS_SEARCH_RESULTS, true);
			this.warningTasks = tasks.map(function (t) {
				var id = TaskShape.toTaskID(t.id),
					cellView = paper.findViewByModel(id);
				cellView.vel.toggleClass(CLASS_SEARCH_RESULT, true);
				return id;
			});
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