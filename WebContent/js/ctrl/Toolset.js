/**
 * A set of tools for interacting with a diagram.
 * Each tool can be activated and deactivated in a diagram
 * and specifies what to do when:
 * - a blank space is clicked
 * - a task is clicked
 * 
 * These tools only encapsulate the logic without
 */
define([
	'ctrl/Diagram'
], function (
	Diagram
) {
	
	return {
		'add-task': {
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
		},
		'add-precedence': {
			title: 'Add precedence',
			requiresSelectedTask: true,
			editMode: {
				className: 'add-precedence',
				listeners: {
					'task:select': function (newTaskView, oldTaskView) {
						if (newTaskView && oldTaskView) {
							var data = { kind: 'PRECEDENCE', scope: 'TASK' };
							this.addPrecedence(oldTaskView, newTaskView, data);
						}
						this.resetEditMode();
					},
					'blank:pointerclick': Diagram.prototype.resetEditMode
				}
			}
		},
		'remove-selected-task': {
			title: 'Remove selected task',
			requiresSelectedTask: true,
			action: Diagram.prototype.removeSelectedTask
		},
		'duplicate-selectedTask': {
			title: 'Duplicate selected task',
			requiresSelectedTask: true,
			action: null	// TODO
		}
	};
	
});