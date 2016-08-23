define([
    'jquery',
    'lib/lodash',
    'lib/backbone',
	'lib/bootstrap-table'
], function (
	$,
	_,
	Backbone
) {
	'use strict';
	
	function createTableToolbar(tableToolbarConfig) {
		var $toolbar = $('<div class="table-toolbar">');
		tableToolbarConfig.buttons.forEach(function (buttonDef) {
			createButton(buttonDef).appendTo($toolbar);
		});
		return $toolbar;
	}
	
	function createRowToolbar(rowToolbarConfig) {
		return {
			title: rowToolbarConfig.title,
			events: rowToolbarConfig.events,
			searchable: rowToolbarConfig.searchable,
			formatter: createRowToolbarFormatter(rowToolbarConfig.buttons)
		};
	}
	
	function createRowToolbarFormatter(buttons) {
		var buttonFormatters = [],
			constant = true;
		buttons.forEach(function (buttonDef) {
			if (typeof buttonDef === 'function') {
				constant = false;
				buttonFormatters.push(_.flow(buttonDef, createButton));
			}
			else
				buttonFormatters.push(_.constant(createButton(buttonDef)));
		});
		var rowToolbarFormatter = wrapButtonFormatters(buttonFormatters);
		return constant ? _.once(rowToolbarFormatter) : rowToolbarFormatter;
	}
	
	function wrapButtonFormatters(buttonFormatters) {
		return function (value, rowData, index) {
			var $wrapper = $('<div>');
			buttonFormatters.forEach(function (f) {
				f(rowData, index).appendTo($wrapper);
			});
			return $wrapper.html()
		};
	}
	
	function createButton(buttonDef) {
		var key = buttonDef.key,
			tag = buttonDef.tagName || 'button',
			title = buttonDef.title || _.capitalize(key),
			classes = buttonDef.classes || 'btn btn-default',
			attrs = buttonDef.attrs || {};
		
		return $('<' + tag + '>').addClass(key).addClass(classes).attr(attrs).text(title);
	}
	
	return Backbone.View.extend({
		initialize: function (config) {
			var $title = $('<h2>').text(config.title).appendTo(this.$el),
				$table = $('<table class="table table-striped">').appendTo(this.$el);
			if (config.tableToolbar)
				config.bootstrap.toolbar = createTableToolbar(config.tableToolbar);
			if (config.rowToolbar)
				config.bootstrap.columns.push(createRowToolbar(config.rowToolbar))
			$table.bootstrapTable(config.bootstrap)
		}
	});

});