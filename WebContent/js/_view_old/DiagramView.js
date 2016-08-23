define([
	'jquery',
	'lib/lodash',
	'lib/joint',
	'ctrl/DiagramPaper',
	'ctrl/DiagramToolbar',
	'ctrl/DiagramToolset'
], function (
	$,
	_,
	joint,
	DiagramPaper,
	DiagramToolbar,
	DiagramToolset
) {
	'use strict';

	return Backbone.View.extend({
		initialize: function (graph) {
			var $paperEl = $('#paper');
			var paper = new joint.dia.Paper({
                el: $paperEl,
                width: $paperEl.width(),
                height: $paperEl.height(),
                gridSize: 2,
                model: graph,
                multiLinks: false,
                restrictTranslate: function (cellView) {
		        	return cellView.model.get('type') !== 'precise.ConstructionUnitShape'
		        		&& this.getArea();
		        }
            });
			this.diaPaper = new DiagramPaper(graph);
			DiagramToolbar.initHtmlToolbar('#toolbar', DiagramToolset, this.diaPaper);
			// Fire 'task:select' with null, undefined for initializing listeners
			this.diaPaper.selectTask(null);
		}
	});
	
});