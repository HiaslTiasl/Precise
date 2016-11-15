define([
	'lib/joint',
	'ctrl/DiagramPaper'
], function (
	joint,
	DiagramPaper
) {
	'use strict';
	
	var DependencyShapeView = joint.shapes.precise.DependencyShapeView;
	
	return [ {
		title: 'Add task',
		requiresSelected: false,
		editMode: {
			className: 'add-task',
			listeners: {
				'blank:pointerdown': function (event, x, y) {
					this.trigger('task:new', {
						position: { x: x, y: y }
					});
				}
			}
		},
	}, {
		title: 'Remove',
		requiresSelected: true,
		action: function () {
			if (this.selectedView)
				this.trigger('cell:delete', this.selectedNS, this.selectedView.model.get('data'));
		}
	}, {
		title: 'Add dependency',
		requiresSelected: 'task',
		editMode: {
			className: 'add-dependency',
			listeners: {
				'link:pointerdown': function () {
					this.resetEditMode();
				},
				'element:pointerdown': function (targetView) {
					var sourceView = this.selectedView;
					if (sourceView && this.selectedNS === 'task') {
						var source = sourceView.model.get('data'),
							target = targetView.model.get('data'),
							changedData = {
								source: source,
								target: target
							};
						if (source === target)
							changedData.vertices = DependencyShapeView.computeLoopVertices(sourceView);
						this.resetEditMode();
						this.trigger('dependency:new', changedData);
					}
				},
				'blank:pointerdown': function (event, x, y) {
					var sourceView = this.selectedView;
					if (sourceView && this.selectedNS === 'task') {
						this.resetEditMode();
						this.trigger('dependency:new', {
							source: sourceView.model.get('data'),
							targetVertex: { x: x, y: y }
						});
					}
				}
			}
		}
//	}, {
//		title: 'Duplicate selected task',
//		requiresSelected: 'task(TODO)',
//		action: null	// TODO
	} ];

});