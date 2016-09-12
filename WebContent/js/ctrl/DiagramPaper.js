define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape'
], function (
	$,
	_,
	joint,
	TaskShape,
	DependencyShape
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
			'blank:pointerclick': function () {
				this.unselect();
			}
		}
	};
	
	var padding = {
		top: 50,
		right: 70,
		bottom: 70,
		left: 50
	}
	
	function DiagramPaper(paper) {
		this.paper = paper;
		//this.selectedView;
		//this.selectedNS;
		//this.editMode;
		this.options = {
			fitToContent: {
				allowNewOrigin: 'any',
				padding: padding,
				minWidth: null,
				minHeight: null
			},
			scaleContentToFit: {
				fittingBBox: {
					x: null,
					y: null,
					width: null,
					height: null
				}
			}
		};
		this.attachListeners();
		this.setEditMode(defaultEditMode);
	}
	
	DiagramPaper.prototype = Object.create(Backbone.Events);
	DiagramPaper.prototype.constructor = DiagramPaper;

	DiagramPaper.prototype.fitToContentOptions = function () {
		var opt = this.options.fitToContent,
			pad = opt.padding || 0,
			$wrapper = this.paper.$el.parent();
		opt.minWidth = $wrapper.width() - (pad.left || 0) - (pad.right || 0);
		opt.minHeight = $wrapper.height() - (pad.top || 0) - (pad.bottom || 0);
		return opt;
	};
	
	DiagramPaper.prototype.scaleContentToFitOptions = function () {
		var opt = this.options.scaleContentToFit,
			pad = this.options.fitToContent.padding || 0,
			$wrapper = this.paper.$el.parent();
		opt.x = pad.left || 0;
		opt.y = pad.top || 0;
		opt.width = $wrapper.width() - (pad.left || 0) - (pad.right || 0);
		opt.height = $wrapper.height() - (pad.top || 0) - (pad.bottom || 0);
		return opt;
	};
	
	DiagramPaper.prototype.attachListeners = function (selector) {
		// Proxy all paper events
		this.listenTo(this.paper, 'all', this.trigger);
		// Selecting cells
		this.paper.on('cell:pointerup', this.onCellPointerup, this);
		// React on internal operations
		this.paper.model
			//.on('add remove', this.fitPaperToContent, this)
			.on('remove', this.onRemove, this)
			.on('batch:stop', this.onBatchStop, this);
	};
	
	DiagramPaper.prototype.onCellPointerup = function (cellView, event, x, y) {
		this.select(cellView.model.graph && cellView);
	};
	
	DiagramPaper.prototype.onRemove = function (cell) {
		if (!cell.removedRemotely) {
			if (this.selectedView && this.selectedView.model === cell)
				this.select(null, this.selectedNS);
			this.triggerNS('remove', typeToNs[cell.get('type')], [cell.get('data')])
		}
	};
	
	DiagramPaper.prototype.onBatchStop = function (options) {
		var cell = options.other && options.other.cell;
		if (cell) {
			var data = cell.get('data'),
				changedData,
				changedNS;
			switch (options.batchName) {
			case 'vertices-change':
				changedData = this.changedVerticesData(cell, data);
				changedNS = NS_DEPENDENCY;
				break;
			case 'end-change':
				changedData = this.changedEndData(cell, data);
				changedNS = NS_DEPENDENCY;
				break;
			case 'position-change':
				changedData = this.changedPositionData(cell, data);
				changedNS = NS_TASK;
				break;
			}
			if (changedData) {
				this.triggerNS('change', changedNS, [_.defaults(changedData, data)]);	
				//this.fitPaperToContent();
			}
		}
	};
	
	DiagramPaper.prototype.changedVerticesData = function (cell, data) {
		var vertices = cell.get('vertices');
		return vertices === data.vertices ? null : { vertices: cell.get('vertices') };
	},
	
	DiagramPaper.prototype.changedEndData = function (cell, data) {
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
	
	DiagramPaper.prototype.changedPositionData = function (cell, data) {
		var position = cell.get('position');
		return position === data.position ? null : { position: position };
	},
	
	DiagramPaper.prototype.triggerNS = function (eventName, ns, args) {
		var nsEvent = ns + ':' + eventName,
			defaultEvent = NS_DIAGRAM + ':' + eventName;
		args.unshift(nsEvent);
		this.trigger.apply(this, args);
		args[0] = ns;
		args.unshift(defaultEvent);
		this.trigger.apply(this, args);
	};
	
	DiagramPaper.prototype.updateDimensions = function () {
		this.paper.setDimensions(this.paper.$el.width(), this.paper.$el.height());
	};
	
	DiagramPaper.prototype.unselect = function () {
		this.select(null, this.selectedNS);
	};
	
	DiagramPaper.prototype.select = function (newView, namespace) {
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
	};
	
	DiagramPaper.prototype.updateSelected = function (data) {
		this.selectedView.model.set('data', data);
	};
	
	DiagramPaper.prototype.getModelID = function (ns, data) {
		return ns === NS_TASK
			? TaskShape.toTaskID(data.id)
			: DependencyShape.toDependencyID(data.id);
	};
	
	DiagramPaper.prototype.addCell = function (ns, data) {
		var args = {
				id: this.getModelID(ns, data), 
				data: data
			},
			cell = ns === NS_TASK ? new TaskShape(args) : new DependencyShape(args);
		this.paper.model.addCell(cell);
		this.select(this.paper.findViewByModel(cell), ns);
	};
	
	DiagramPaper.prototype.removeCell = function (ns, data) {
		this.resetEditMode();
		var model = this.paper.model.getCell(this.getModelID(ns, data));
		if (model) {
			model.removedRemotely = true;
			model.remove();
			if (model === this.selectedView.model)
				this.select(null, ns);
		}
	};
	
	DiagramPaper.prototype.fromJSON = function (rawGraph) {
		this.paper.model.fromJSON(rawGraph);
		this.onRenderDone(function () {
			//this.scaleContentToFitPaper();
			//this.fitPaperToContent(/*_.assign({}, this.fitToContentOptions(), this.scaleContentToFitOptions())*/);
		}, this);
	};
	
	DiagramPaper.prototype.fitPaperToContent = function (opt) {
		this.paper.fitToContent(opt || this.fitToContentOptions());
	};
	
	DiagramPaper.prototype.scaleContentToFitPaper = function (opt) {
		this.paper.scaleContentToFit(opt || this.scaleContentToFitOptions());
	};
	
	DiagramPaper.prototype.onRenderDone = function (callback, thisArg, async) {
		if (async != null ? async : this.paper.options.async)
			this.paper.once('render:done', callback, thisArg);
		else
			callback.call(thisArg);
	};
	
	DiagramPaper.prototype.setEditMode = function (editMode) {
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
	};
	
	DiagramPaper.prototype.resetEditMode = function () {
		this.setEditMode(defaultEditMode);
	};
	
	DiagramPaper.prototype.toggleEditMode = function (editMode) {
		this.setEditMode(this.editMode !== editMode ? editMode : defaultEditMode);
	};
	
	DiagramPaper.prototype.invokeTool = function (toolDef) {
		if (toolDef.action)
			toolDef.action.call(this);
		if (toolDef.editMode)
			this.toggleEditMode(toolDef.editMode);
	};
	
	DiagramPaper.prototype.canInvokeTool = function (toolDef) {
		var reqSelView = toolDef.requiresSelected,
			reqSelNS = typeof reqSelView === 'string' && reqSelView,
			selView = this.selectedView,
			selNS = this.selectedNS;
		return !reqSelView || (selView && (!reqSelNS || reqSelNS === selNS));
	};
	
	return DiagramPaper;
	
});