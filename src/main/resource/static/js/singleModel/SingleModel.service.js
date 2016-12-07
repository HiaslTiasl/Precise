define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	SingleModelService.$inject = ['Models'];
	
	function SingleModelService(Models) {
		
		this.findByName = Models.findByName;
		this.findSummaryByName = findByNameWithPartInfos;
		
		function findByNameWithPartInfos() {
			return Models.findByName({ projection: 'modelSummary' });
		}
		
	}
	
	return SingleModelService;
	
});