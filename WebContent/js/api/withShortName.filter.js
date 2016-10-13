define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	function withShortNameFilterFactory() {
		
		function withShortName(object, nameKey, shortNameKey) {
			var name = _.get(object, nameKey || 'name'),
				shortName = _.get(object, shortNameKey || 'shortName');
			return shortName && shortName !== name
				? name + ' (' + shortName + ')'
				: name;
		}
		
		return withShortName;
		
	}
	
	return withShortNameFilterFactory;
	
});