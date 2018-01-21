/**
 * Tool definitions for the buttons in the Edit toolset of the diagram.
 * @module "diagramPaper/DiagramPaper.editTools"
 */
define([
	'lib/joint',
	'ctrl/DiagramPaper'
], function (
	joint,
	DiagramPaper
) {
	'use strict';
	
	var DependencyShapeView = joint.shapes.precise.DependencyShapeView;
	
	return [
        // Enter in the add-task edit mode
	    {
			title: 'Add task',
			requiresSelected: false,	// No selection required for adding tasks
			editMode: {
				className: 'add-task',
				listeners: {
					/**
					 * There was a click on the paper while this edit mode was active,
					 * so trigger an event that requests the addition of a new task at
					 * the click position.
					 */
					'blank:pointerdown': function (event, x, y) {
						this.triggerNS('new', 'task', [{
							position: { x: x, y: y }
						}]);
					}
				}
			},
		},
		// Remove the currently selected cell, which can be both either a task or a dependency
		{
			title: 'Remove',
			requiresSelected: true,		// Only enable this tool if something is selected
			/**
			 * The tool was invoked, so trigger an event that requests the deletion
			 * of the currently selected cell.
			 */
			action: function () {
				if (this.selectedView)
					this.triggerNS('delete', this.selectedNS, [this.selectedView.model.get('data')]);
			}
		},
		// Add a new dependency from the currently selected task to the one which is selected next
		{
			title: 'Add dependency',
			requiresSelected: 'task',	// Only enable this tool if a task is currently selected
			editMode: {
				className: 'add-dependency',
				listeners: {
					/**
					 * There was a click on (another) dependency, so cancel the addition
					 * and reset the edit mode.
					 */
					'link:pointerdown': function () {
						this.resetEditMode();
					},
					/**
					 * There was a click on a task, so trigger an event that requests
					 * the addition of a dependency from the currently selected task
					 * to the task that was clicked.
					 */
					'element:pointerdown': function (targetView) {
						var sourceView = this.selectedView;
						if (sourceView && this.selectedNS === 'task') {
							var source = sourceView.model.get('data'),
								target = targetView.model.get('data'),
								changedData = {
									source: source,
									target: target
								};
							// Make loops visible
							if (source === target)
								changedData.vertices = DependencyShapeView.computeLoopVertices(sourceView);
							this.resetEditMode();
							this.triggerNS('new', 'dependency', [changedData]);
						}
					},
					/**
					 * There was a click on the blank paper, so trigger an event that
					 * requests the addition of a dependency from the currently selected
					 * task to the click position.
					 */
					'blank:pointerdown': function (event, x, y) {
						var sourceView = this.selectedView;
						if (sourceView && this.selectedNS === 'task') {
							this.resetEditMode();
							this.triggerNS('new', 'dependency', [{
								source: sourceView.model.get('data'),
								targetVertex: { x: x, y: y }
							}]);
						}
					}
				}
			}
		},
		// Duplicate the currently selected task
		{
			title: 'Duplicate task',
			requiresSelected: 'task',
			action: function () {
				// Use data of selected task and translate by (10,10)
				var oldData = this.selectedView.model.get('data'),
					newData = _.defaults({
						position: {
							x: oldData.position.x + 50,
							y: oldData.position.y + 50
						}
					}, oldData);
				this.triggerNS('new', 'task', [newData]);
			}
		}
	];

});