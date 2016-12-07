define([
	'lib/lodash',
	'lib/joint',
	'Util',
	'lib/tooledViewPlugin'
], function (
	_,
	joint,
	Util
) {
	Util.set(joint.plugins, 'precise.TaskToolsShape', _.extend({}, joint.plugins.TooledModelInterface, {
		removeTool: true,
		moveTool: false,
		resizeTool: false,
		portsTool: false,
		deleteToolMarkup: [
			'<circle fill="red" r="11"/>',
			'<path transform="scale(.8) translate(-16, -16)" d="M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 16.447,18.746 21.948,24.248z"/>',
			'<title>Remove</title>'
		].join('')
	}));
	Util.set(joint.plugins, 'precise.TaskToolsShapeView', _.extend({}, joint.plugins.TooledViewInterface, {
		renderDeleteTool: function () {
			this.$('.deleteTool')
				.empty()
				.append(this.model.deleteToolMarkup);
		}
	}));
});