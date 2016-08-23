define(['ctrl/DiagramPaper'], function (DiagramPaper) {
	'use strict';
	
	return [ {
		title: 'Add task',
		requiresSelectedTask: false,
		editMode: {
			className: 'add-task',
			listeners: {
				'blank:pointerclick': function (event, x, y) {
					var data = { name: prompt('task name', '<enter name>') };
					this.addTask({ x: x, y: y }, data);
					this.resetEditMode();
				}
			}
		},
	}, {
		title: 'Add dependency',
		requiresSelectedTask: true,
		editMode: {
			className: 'add-dependency',
			listeners: {
				'cell:select': function (newTaskView, oldTaskView) {
					if (DiagramPaper.isTaskView(newTaskView) && DiagramPaper.isTaskView(oldTaskView)) {
						var data = { kind: 'PRECEDENCE', scope: 'TASK' };
						this.addDependency(oldTaskView, newTaskView, data);
					}
					this.resetEditMode();
				},
				'blank:pointerclick': DiagramPaper.prototype.resetEditMode
			}
		}
	}, {
		title: 'Remove selected task',
		requiresSelectedTask: true,
		action: DiagramPaper.prototype.removeSelected
	}, {
		title: 'Duplicate selected task',
		requiresSelectedTask: true,
		action: null	// TODO
	} ];

});