define([
	'jquery',
	'lib/lodash',
], function (
	$,
	_
) {
	'use strict';
	
	function TemplateView(config) {
		this.selector = config.selector;
		this.templateURL = config.templateURL;
		this.init = config.init;
		this.resolve = _.once(TemplateView.prototype.resolve);
	}
	
	TemplateView.create = function (config) {
		return new TemplateView(config);
	};
	
	TemplateView.prototype.resolve = function () {
		var init = this.init;
		return $.ajax({
			url: this.templateURL,
			dataType: 'html'
		}).then(function (el) {
			var $el = $(el);
			return init($el) || $el;
		});
	};
	
	TemplateView.prototype.show = function () {
		var selector = this.selector;
		return this.resolve().then(function ($el) {
			$el.show().appendTo(selector);
		});
	};
	
	TemplateView.prototype.hide = function () {
		return this.resolve().then(function ($el) {
			$el.hide();
		});
	};
	
	return TemplateView;
});