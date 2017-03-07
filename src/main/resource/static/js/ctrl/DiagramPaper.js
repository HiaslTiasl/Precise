/**
 * Wrapper of a JointJS paper representing a RPECISE process model diagram.
 * Acts as a facade to communicate to the diagram and JointJS in general from the
 * outer application in the angular context. 
 * @module "ctrl/DiagramPaper"
 */
define([
    'jquery',
	'lib/lodash',
	'lib/joint',
	'shapes/TaskShape',
	'shapes/DependencyShape',
	'api/hal',
	'util/util'
], function (
	$,
	_,
	joint,
	TaskShape,
	DependencyShape,
	HAL,
	util
) {
	'use strict';
	
	var DependencyShapeView = joint.shapes.precise.DependencyShapeView;
	
	// Event namespaces
	var NS_DIAGRAM = 'diagram',
		NS_TASK = 'task',
		NS_DEPENDENCY = 'dependency';
	
	// CSS classes for switching display mode
	var CLASS_WARNING = 'problem',
		CLASS_SEARCH_RESULTS = 'search-results',
		CLASS_SEARCH_RESULT = 'search-result';
	
	var nsToType = {},	// Map from custom event namespaces to JointJS type strings
		typeToNs;		// Map from JointJS type strings to custom event namespaces
	
	// Fill the above maps
	nsToType[NS_TASK] = TaskShape.prototype.defaults.type;
	nsToType[NS_DEPENDENCY] = DependencyShape.prototype.defaults.type;
	
	typeToNs = _.invert(nsToType);
	
	/**
	 * Edit mode that is active when no edit mode button is active.
	 * Unselects the selected element when the blank paper is clicked. 
	 */
	var defaultEditMode = {
		className: 'default-mode',
		listeners: {
			'blank:pointerdown': function () {
				this.unselect();
			}
		}
	};
	
	/** Extracts an id of the given entity data (task or dependency). */
	function idOfEntity(e) {
		return HAL.resolve(HAL.hrefTo(e));
	}
	
	/**
	 * Represents the paper of a diagram.
	 * @constructor
	 * @extends Backbone.Events
	 */
	var DiagramPaper = util.defineClass(Backbone.Events, {
		
		/** Creates a DiagramPaper. */
		constructor: function DiagramPaper(paper) {
			this.paper = paper;
			//this.selectedView;
			//this.selectedNS;
			//this.editMode;
			//this.problemTasks;
			//this.problemDependencies;
			this.attachListeners();
			this.setEditMode(defaultEditMode);
		},
	
		/** Attaches basic event listeners to paper and graph. */
		attachListeners: function (selector) {
			// Proxy all paper events
			this.listenTo(this.paper, 'all', this.trigger);
			// Selecting cells
			this.listenTo(this.paper, 'cell:pointerup', this.onCellPointerup);
			// React on internal operations
			this.listenTo(this.paper.model, 'remove', this.onRemove)
			this.listenTo(this.paper.model, 'batch:stop', this.onBatchStop)
		},
		
		/** Pointer was released over the given cellView -> select it. */
		onCellPointerup: function (cellView, event, x, y) {
			this.select(cellView.model.graph && cellView);
		},
		
		/** The given cell was removed from the graph -> notify outer application about it if needed. */
		onRemove: function (cell) {
			if (!cell.removedRemotely) {
				// Unselect cell if selected
				if (this.selectedView && this.selectedView.model === cell)
					this.unselect();
				this.triggerNS('remove', typeToNs[cell.get('type')], [cell.get('data')])
			}
		},
		
		/** A batch operation finished -> notify outer application if needed. */
		onBatchStop: function (options) {
			var cell = options.other && options.other.cell;
			if (cell) {
				var data = cell.get('data'),
					changedData,	// resulting operation data
					changedNS;		// namespace of the operation
				// Obtain the data and namespace corresponding to the operation
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
				// Only trigger a notification if the data actually changed
				if (changedData)
					this.triggerNS('change', changedNS, [_.defaults(changedData, HAL.pickLinks(data))]);	
			}
		},
		
		/**
		 * A batch operation that changed the vertices of the given dependency finished.
		 * Returns data containing the change, if any.
		 */
		changedVerticesData: function (options, dependencyCell, data) {
			var cellVertices = dependencyCell.get('vertices'),
				source = dependencyCell.get('source'),
				target = dependencyCell.get('target'),
				newVertices = this.checkLoopVisible(source, target, cellVertices) || cellVertices;
			return newVertices === data.vertices ? null : { vertices: newVertices };
		},
		
		/**
		 * A batch operation that changed an endpoint of the given dependency finished.
		 * Returns data containing the change, if any.
		 */
		changedEndData: function (options, dependencyCell, data) {
			var changedData,
				end = options.other.end,					// A string identifying the direction of the endpoint ('source' or 'target')
				endInfo = DependencyShape.endInfo[end],		// More information on the particular end
				endVal = dependencyCell.get(end);			// The old JointJS value of the endpoint, containing either an ID or coordinates
			if (endVal.id) {
				// The endpoint was moved to a task
				if (endVal.id !== idOfEntity(data[end])) {
					// The new tasks differs from the old one
					// -> add it to the changes
					var endCell = this.paper.model.getCell(endVal.id);
					changedData = {};
					changedData[end] = endCell.get('data');
					// also add new vertices if required to make a loop visible
					var loopVertices = this.checkLoopVisible(endVal, dependencyCell.get(endInfo.opposite), data.vertices);
					if (loopVertices)
						changedData.vertices = loopVertices;
				}
			}
			else if (endVal !== data[endInfo.vertex]) {
				// The enpoint was moved to a (new) coordinate on the paper
				// -> add it to the changes
				changedData = {};
				changedData[end] = null;
				changedData[endInfo.vertex] = endVal;
			}
			return changedData;
		},
		
		/**
		 * A batch operation that changed (moved) the label of the given dependency finished.
		 * Returns data containing the change, if any.
		 */
		changedLabel: function (options, cell, data) {
			var labelPosition = cell.label(0).position;
			return _.isEqual(labelPosition, data.labelPosition) ? null : { labelPosition: labelPosition };
		},
		
		/**
		 * A batch operation that changed the position of the given task finished.
		 * Returns data containing the change, if any.
		 */
		changedPositionData: function (options, cell, data) {
			var position = cell.get('position');
			return _.isEqual(position, data.position) ? null : { position: position };
		},
		
		/**
		 * Checks whether the given source and target form a loop (i.e. refer to the same task)
		 * and whether it has no vertices.
		 * If so, returns a list of vertices that makes the loop visible, null otherwise.
		 */
		checkLoopVisible: function (end1, end2, vertices) {
			if (end1.id !== end2.id || !util.isEmpty(vertices))
				return null;
			var taskView = this.paper.findViewByModel(end1);
			return DependencyShapeView.computeLoopVertices(taskView);
		},
		
		/**
		 * Helper method to trigger an event of a given name under a given namespace first,
		 * and then under a default namespace 'diagram', passing the given namespace as the
		 * next (second) argument.
		 * More arguments can be specified in the args array.
		 * The 
		 * Emits an event of the given name, namespace, and arguments.
		 * Similar to JointJS, we emit two events:
		 * First one that prepends ns and ':' to eventName, and then another one that
		 * prepends 'diagram:' to event name and passes ns as the next argument.
		 * args are passed as the last arguments. 
		 * @example
		 * 	this.triggerNS('event', 'task', [1, 2]);
		 * 	// is equivalent to the following two statements
		 *  this.trigger('task:event', 1, 2);
		 *  this.trigger('diagram':event', 'task', 1, 2);
		 */
		triggerNS: function (eventName, ns, args) {
			// Prepend the namespaced event to the argument list and apply trigger to the result
			args.unshift(ns + ':' + eventName);				
			this.trigger.apply(this, args);
			// Replace the first argument with the eventName under the default namespace
			// and another argument for ns, then apply trigger again
			args[0] = ns; 
			args.unshift(NS_DIAGRAM + ':' + eventName);
			this.trigger.apply(this, args);
		},
		
		/** Resets the selected cell view to null and notify outer application about it. */
		unselect: function () {
			this.select(null, this.selectedNS);
		},
		
		/**
		 * Sets the given cell view of the given namespace as the currently selected one,
		 * and notify outer application about it.
		 * If a namespace is not provided, it is determined from the model data of the
		 * given view. 
		 */
		select: function (newView, namespace) {
			var oldView = this.selectedView;
			// Only do something if the given view differs from the currently selected one
			if (oldView !== newView) {
				var oldNS = this.selectedNS,
					newNS = namespace || (newView && typeToNs[newView.model.get('type')]);
				// Highlight (only) the selected view
				if (oldView)
					oldView.unhighlight();
				if (newView) 
					newView.highlight();
				// Notify application, passing the newly selected view and the previous one
				// iff it was of the same type, null otherwise.
				// Trigger the event before actually applying the change so the old values
				// are available through the this-pointer.
				this.triggerNS('select', newNS, [newView, oldNS === newNS ? oldView : null]);
				this.selectedNS = newNS;
				this.selectedView = newView;
			}
		},
		
		/** Changes the given data in the corresponding graph cell. */
		updateCell: function (data) {
			var model = this.paper.model.getCell(HAL.hrefTo(data));
			if (model)
				model.set('data', data);
		},
		
		/**
		 * Adds a cell of the given namespace using the given data,
		 * and select the resulting view.
		 */
		addCell: function (ns, data) {
			var args = {
					id: HAL.hrefTo(data), 
					data: data
				},
				cell = ns === NS_TASK ? new TaskShape(args) : new DependencyShape(args);
			this.paper.model.addCell(cell);
			this.select(this.paper.findViewByModel(cell), ns);
		},
		
		/** Removes the cell of the given namespace and data. */
		removeCell: function (ns, data) {
			this.resetEditMode();
			var model = this.paper.model.getCell(HAL.hrefTo(data));
			if (model) {
				// TODO: check if remove({silent: true}) would work as well.
				model.removedRemotely = true;
				model.remove();
				if (model === this.selectedView.model)
					this.select(null, ns);
			}
		},
		
		/** Resets the graph cells to the ones in the given JSON object. */
		fromJSON: function (rawGraph) {
			this.paper.model.fromJSON(rawGraph);
		},
		
		/** Hides locations iff hideLocations is set. */
		toggleHideLocations: function (hideLocations) {
			this.paper.model.getElements().forEach(function (taskCell, i) {
				taskCell.set('hideLocations', hideLocations);
			});
		},
		
		/** Hides dependency labels iff hideLabels is set. */
		toggleHideLabels: function (hideLabels) {
			this.paper.model.getLinks().forEach(function (taskCell, i) {
				taskCell.set('hideLabels', hideLabels);
			});
		},
		
		/** Sets the given class to the elements of the given cellIDs iff toggle is set. */
		toggleEntityClass: function (cellIDs, clazz, toggle) {
			var paper = this.paper;
			_.forEach(cellIDs, function (id) {
				paper.findViewByModel(id).vel.toggleClass(clazz, toggle);
			});
		},
		
		/** Sets the given class to the elements corresponding to the given locations iff toggle is set. */
		toggleLocationClass: function (locs, clazz, toggle) {
			var paper = this.paper;
			_.forEach(locs, function (l) {
				paper.findViewByModel(idOfEntity(l.task))
					.vel.find('.loc-num-' + l.index).forEach(function (v) {
						v.toggleClass(clazz, toggle);
					});
			});
		},
		
		/** Unhighlights problems and search results, if any. */
		resetClasses: function () {
			this.resetProblems();
			this.resetSearchResults();
		},
		
		/** Unhighlights problems, if any. */
		resetProblems: function () {
			var paper = this.paper;
			if (this.problemEntities) {
				this.toggleEntityClass(this.problemEntities, CLASS_WARNING, false);
				this.problemEntities = null;
			}
			if (this.problemLocations) {
				this.toggleLocationClass(this.problemLocations, CLASS_WARNING, false);
				this.problemEntities = null;
			}
		},
		
		/** Unhighlights search results, if any. */
		resetSearchResults: function () {
			this.paper.$el.toggleClass(CLASS_SEARCH_RESULTS, false);
			if (this.searchResults) {
				this.toggleEntityClass(this.searchResults, CLASS_SEARCH_RESULT, false);
				this.searchResults = null;
			}
		},
		
		/** Highlights the shapes involved in the given problem. */
		showProblem: function (problem) {
			this.resetClasses();
			var paper = this.paper,
				graph = paper.model;
			
			// Highlight involved entities (tasks and dependencies)
			this.problemEntities = _.map(problem.entities, idOfEntity);
			this.toggleEntityClass(this.problemEntities, CLASS_WARNING, true);
			
			// Highlights involved locations
			this.problemLocations = problem.locations;
			this.toggleLocationClass(this.problemLocations, CLASS_WARNING, true);
		},
		
		/** Highlights the given tasks as search result. */
		showSearchResults: function (tasks) {
			var paper = this.paper;
			this.resetClasses();
			// Add a CSS class to the paper to indicate that we are displaying a search result
			paper.$el.toggleClass(CLASS_SEARCH_RESULTS, true);
			// Highlight search results
			this.searchResults = _.map(tasks, idOfEntity);
			this.toggleEntityClass(this.searchResults, CLASS_SEARCH_RESULT, true);
		},

		/**
		 * Sets the given edit mode.
		 * An edit mode is an object with the following properties:
		 * <ul>
		 * <li>className: a CSS class name to be set for the paper as long as the edit mode is active.
		 * <li>listeners: an object that maps event names to handler functions, which are
		 * attached to this DiagramPaper and invoked with the same DiagramPaper as the this pointer.
		 * </ul>
		 */
		setEditMode: function (editMode) {
			// Only do something if the edit mode actually changes
			if (editMode !== this.editMode) {
				if (this.editMode) {
					// Remove old edit mode
					this.off(this.editMode.listeners, this);
					if (this.editMode.className)
						this.paper.$el.removeClass(this.editMode.className);
				}
				if (editMode) {
					// Set new edit mode
					this.on(editMode.listeners, this);
					if (editMode.className)
						this.paper.$el.addClass(editMode.className);
				}
				this.editMode = editMode;
			}
		},
		
		/** Resets the edit mode to the default one. */
		resetEditMode: function () {
			this.setEditMode(defaultEditMode);
		},
		
		/** Toggles the given edit mode: reset it if already set, set it otherwise. */
		toggleEditMode: function (editMode) {
			this.setEditMode(this.editMode !== editMode ? editMode : defaultEditMode);
		},
		
		/**
		 * Invokes the given tool, which corresponds to buttons.
		 * A tool is an object with the following properties:
		 * <ul>
		 * <li>title: the title of the tool, i.e. the display name of the corresponding button.
		 * <li>requiresSelected: a boolean indicating whether a cell must be selected in order
		 * for the tool to be enabled, or a string indicating the kind of shape that must be 
		 * selected ('task' or 'dependency'). Default is false.
		 * <li>editMode: An edit mode that is toggled when the tool is invoked.
		 * <li>action: a function that is invoked with this DiagramPaper as the this-pointer.
		 * </ul>
		 */
		invokeTool: function (toolDef) {
			if (toolDef.action)
				toolDef.action.call(this);
			if (toolDef.editMode)
				this.toggleEditMode(toolDef.editMode);
		},
		
		/** Determines whether the given tool can be invoked according to its requiresSelected value. */
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