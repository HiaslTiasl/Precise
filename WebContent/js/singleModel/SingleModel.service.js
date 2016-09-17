define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelService.$inject = ['Models'];
	
	function SingleModelService(Models) {
		
		this.findByName = _.memoize(Models.findByName);
		this.cache = this.findByName.cache;
		
	}
	
	return SingleModelService;
	
});