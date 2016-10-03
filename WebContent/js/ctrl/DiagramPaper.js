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
			this.paper.model.getCells().forEach(function (cell) {
				cell.set('hideLocations', hideLocations);
			});
		},
		
		resetWarnings: function () {
			var paper = this.paper;
			if (this.warningTasks) {
				this.warningTasks.forEach(function (t) {
					paper.findViewByModel(TaskShape.toTaskID(t.id)).vel.toggleClass('warning', false);
				});
				this.warningTasks = null;
			}
			if (this.warningDependencies) {
				this.warningDependencies.forEach(function (d) {
					paper.findViewByModel(DependencyShape.toDependencyID(d.id)).vel.toggleClass('warning', false);
				});
				this.warningDependencies = null;
			}
		},
		
		showWarningForTasks: function (tasks, withDependencies) {
			this.resetWarnings();
			var paper = this.paper,
				graph = paper.model;
			tasks.forEach(function (t) {
				var id = TaskShape.toTaskID(t.id),
					cell = graph.getCell(id),
					cellView = paper.findViewByModel(id);
				cellView.vel.toggleClass('warning', true)
			});
			this.warningTasks = tasks;
			if (withDependencies) {
				var dependencies = [];
				tasks.forEach(function (t) {
					graph.getConnectedLinks(graph.getCell(TaskShape.toTaskID(t.id)), {
						outbound: true,
						inbound: false
					}).forEach(function (d) {
						var target = d.get('target');
						if (target.id) {
							var targetView = paper.findViewByModel(target.id);
							if (targetView.vel.hasClass('warning')) {
								paper.findViewByModel(d).vel.toggleClass('warning', true);
								dependencies.push(d.get('data'));
							}
						}
					});
				})
				this.warningDependencies = dependencies;
			}
		},
		
		toggleWarningClass: function (cellView, warning) {
			cellView.vel.toggleClass('warning', warning);
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