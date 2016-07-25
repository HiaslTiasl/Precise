define([
	'jquery',
	'lib/lodash'
], function (
	$,
	_
) {
	'use strict';
	
	return {
		initHtmlToolbar: function (selector, toolset, diagram) {
			var toolbarButtons = _.mapValues(toolset, function (def, name) {
				return $('<button>')
					.text(def.title)
					.toggleClass('require-selected-task', def.requiresSelectedTask)
					.data('toolName', name)
					.appendTo('#toolbar')
			});
			
			$(selector).first().on('click', 'button', function () {
				diagram.invokeTool(toolset[$(this).data('toolName')]);
			});
			
			diagram.on('task:select', function (newTask, oldTask) {
				var disable = !newTask;
				if (oldTask === undefined || disable !== !oldTask)
					$('#toolbar .require-selected-task').prop('disabled', disable)
			});
		}
	};
})