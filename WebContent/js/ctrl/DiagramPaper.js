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
	
	var VIEW_TYPE_TASK = 'task',
		VIEW_TYPE_DEPENDENCY = 'dependency';
	
	function getOtherType(type) {
		return type === VIEW_TYPE_TASK ? VIEW_TYPE_DEPENDENCY : VIEW_TYPE_TASK;
	}
	
	function DiagramPaper(paper) {
		this.paper = paper;
		//this.editMode;
		//this.selectedView;
		//this.selectedViewType;
		this.attachListeners();
	}
	
	DiagramPaper.prototype = Object.create(Backbone.Events);
	DiagramPaper.prototype.constructor = DiagramPaper;

	DiagramPaper.isTaskView = function (cellView) {
		return cellView && cellView.model.get('type') === 'precise.TaskShape';
	};
	
	DiagramPaper.prototype.attachListeners = function (selector) {
		
		// Proxy all paper events
		this.listenTo(this.paper, 'all', this.trigger);
		
		// Update selected task
		this.paper.on('blank:pointerclick', function (event, x, y) {
			this.select(this.selectedViewType, null);
		}, this)
		.on('element:pointerup', function (cellView, event, x, y) {
			this.select('task', cellView);
		}, this)
		.on('link:pointerup', function (cellView, event, x, y) {
			this.select('dependency', cellView);
		}, this);
	};
	
	DiagramPaper.prototype.updateDimensions = function () {
		this.paper.setDimensions(this.paper.$el.width(), this.paper.$el.height());
	};
	
	DiagramPaper.prototype.select = function (type, newView) {
		var oldView = this.selectedView;
		if (newView !== oldView) {
			var oldType = this.selectedViewType;
			if (oldView)
				oldView.unhighlight();
			if (newView) 
				newView.highlight();
			this.trigger(type + ':select', newView, oldType === type ? newView : null);
			this.selectedViewType = type;
			this.selectedView = newView;
		}
	};
	
	DiagramPaper.prototype.updateSelected = function (data) {
		this.selectedView.model.set('data', data);
		this.select('task', null);
	};
	
	DiagramPaper.prototype.addTask = function (position, data) {
		var task = new TaskShape({
			position: position,
			data: data
		});
		this.paper.model.addCell(task);
		this.select('task', this.paper.findViewByModel(task));
	};
	
	DiagramPaper.prototype.addDependency = function (sourceView, targetView, data) {
		this.paper.model.addCell(new DependencyShape({
			source: { id: sourceView.model.id }, 
			target: { id: targetView.model.id },
			data: data
		}));
	};
	
	DiagramPaper.prototype.removeSelected = function () {
		this.resetEditMode();
		if (this.selectedViews)
			this.selectedViews.remove();
	};
	
	DiagramPaper.prototype.fromJSON = function (rawGraph) {
		this.paper.model.fromJSON(rawGraph);
	};
	
	DiagramPaper.prototype.setEditMode = function (editMode) {
		if (!editMode)
			this.resetEditMode();
		else {
			this.on(editMode.listeners, this);
			this.paper.$el.addClass(editMode.className);
			this.editMode = editMode;
		}
	};
	
	DiagramPaper.prototype.resetEditMode = function () {
		if (this.editMode) {
			this.off(this.editMode.listeners);
			this.paper.$el.removeClass(this.editMode.className);
			this.editMode = null;
		}
	};
	
	DiagramPaper.prototype.toggleEditMode = function (editMode) {
		this.resetEditMode();
		if (this.editMode === editMode)
			this.resetEditMode();
		else
			this.setEditMode(editMode);
	};
	
	DiagramPaper.prototype.invokeTool = function (toolDef) {
		if (toolDef.action)
			toolDef.action.call(this);
		if (toolDef.editMode)
			this.toggleEditMode(toolDef.editMode);
	};

//	paper.scaleContentToFit({
//		preserveAspectRatio: true,
//	});
	
	return DiagramPaper;
	
});