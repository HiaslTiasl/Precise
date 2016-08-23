define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/LocationShape',
	'shapes/DependencyShape'
], function (
	$,
	_,
	joint,
	TaskShape,
	LocationShape,
	DependencyShape
) {
	
	function DiagramPaper(paper) {
		this.paper = paper;
		this.attachListeners();
		//this.editMode;
		//this.selectedView
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
			this.select(null);
		}, this)
		.on('cell:pointerclick', function (cellView, event, x, y) {
			this.select(cellView);
		}, this);
	};
	
	DiagramPaper.prototype.updateDimensions = function () {
		this.paper.setDimensions(this.paper.$el.width(), this.paper.$el.height());
	};
	
	DiagramPaper.prototype.select = function (cellView) {
		if (cellView !== this.selectedView) {
			if (this.selectedView)
				this.selectedView.unhighlight();
			if (cellView) {
				cellView.highlight();
				cellView.model.toFront({ deep: true });
			}
			this.trigger('cell:select', cellView, this.selectedView);
			this.selectedView = cellView;
		}
	};
	
	DiagramPaper.prototype.addTask = function (position, data) {
		var task = new TaskShape({
			position: position,
			data: data
		});
		this.paper.model.addCell(task);
		this.select(this.paper.findViewByModel(task));
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
		if (this.selectedView)
			this.selectedView.remove();
	};
	
	DiagramPaper.prototype.fromJSON = function (rawGraph) {
		this.paper.model.fromJSON(rawGraph);
		this.checkParents();
	};
	
	DiagramPaper.prototype.checkParents = function () {
		var graph = this.paper.model,
			elements = graph.getElements();
		if (elements) {
			elements.forEach(function (e) {
				var parent = graph.getCell(e.get('parent'));
				if (parent)
					parent.embed(e);
			});
		}
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