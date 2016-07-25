define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/ConstructionUnitShape',
	'shapes/Precedence'
], function (
	$,
	_,
	joint,
	TaskShape,
	ConstructionUnitShape,
	Precedence
) {
	
	function Diagram() {
		this.graph = new joint.dia.Graph();
		//this.editMode;
		//this.selectedTask
	}
	
	Diagram.prototype = Object.create(Backbone.Events);
	Diagram.prototype.constructor = Diagram;

	Diagram.isTaskView = function (cellView) {
		return cellView.model.get('type') === 'precise.TaskShape';
	};
	
	Diagram.prototype.initPaper = function (selector) {
		var $paper = $(selector),
			paper = new joint.dia.Paper({
	        el: $paper,
	        width: $paper.width(),
	        height: $paper.height(),
	        model: this.graph,
	        gridSize: 2,
	        restrictTranslate: function (cellView) {
	        	if (cellView.model.get('type') === 'precise.ConstructionUnitShape') {
	        		var pos = cellView.model.get('position'),
	        			parentBBox = this.model.getCell(cellView.model.get('parent')).getBBox();
	        		return _.extend(parentBBox, { y: pos.y, height: 0 });
	        	}
	        }
	    });
		
		// Proxy all paper events
		this.listenTo(paper, 'all', this.trigger);
		
		// Update dimensions
		var resizeHandler = function () {
			
		};
		
		// Update selected task
		paper.on('blank:pointerclick', function (event, x, y) {
			this.selectTask(null);
		}, this)
		.on('cell:pointerclick', function (cellView, event, x, y) {
			cellView.model.toFront();
			if (Diagram.isTaskView(cellView))
				this.selectTask(cellView);				
		}, this);
		
		this.paper = paper;
	};
	
	Diagram.prototype.updateDimensions = function () {
		this.paper.setDimensions(this.paper.$el.width(), this.paper.$el.height());
	};
	
	Diagram.prototype.selectTask = function (cellView) {
		if (cellView !== this.selectedTask) {
			if (this.selectedTask)
				this.selectedTask.unhighlight();
			if (cellView)
				cellView.highlight();
			this.trigger('task:select', cellView, this.selectedTask);
			this.selectedTask = cellView;
		}
	};
	
	Diagram.prototype.addTask = function (position, data) {
		var task = new TaskShape({
			position: position,
			data: data
		});
		this.graph.addCell(task);
		this.selectTask(this.paper.findViewByModel(task));
	};
	
	Diagram.prototype.addPrecedence = function (sourceView, targetView, data) {
		this.graph.addCell(new Precedence({
			source: { id: sourceView.model.id }, 
			target: { id: targetView.model.id },
			data: data
		}));
	};
	
	Diagram.prototype.removeSelectedTask = function () {
		this.resetEditMode();
		if (this.selectedTask)
			this.selectedTask.remove();
	};
	
	Diagram.prototype.setEditMode = function (editMode) {
		if (!editMode)
			this.resetEditMode();
		else {
			this.on(editMode.listeners, this);
			this.paper.$el.addClass(editMode.className);
			this.editMode = editMode;
		}
	};
	
	Diagram.prototype.resetEditMode = function () {
		if (this.editMode) {
			this.off(this.editMode.listeners);
			this.paper.$el.removeClass(this.editMode.className);
			this.editMode = null;
		}
	};
	
	Diagram.prototype.toggleEditMode = function (editMode) {
		this.resetEditMode();
		if (this.editMode === editMode)
			this.resetEditMode();
		else
			this.setEditMode(editMode);
	};
	
	Diagram.prototype.invokeTool = function (toolDef) {
		if (toolDef.action)
			toolDef.action.call(this);
		if (toolDef.editMode)
			this.toggleEditMode(toolDef.editMode);
	};

//	paper.scaleContentToFit({
//		preserveAspectRatio: true,
//	});
	
	return Diagram;
	
});