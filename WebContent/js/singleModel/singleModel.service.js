define([
], function () {
	'use strict';
	
	SingleModelService.$inject = ['preciseApi'];
	
	function SingleModelService(preciseApi) {
		
		this.findByName = function (name) {
			return preciseApi.fromBase()
				.traverse(function (builder) {
					return builder.follow('models', 'search', 'findByName')
						.withTemplateParameters({ name: name })
						.get()
				});
		};
	}
	
	return SingleModelService;
	
});