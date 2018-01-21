/**
 * Angular filter for displaying resources with a name and a short name.
 * @module "api/withShortName.filter"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	/** Creates the filter. */
	function withShortNameFilterFactory() {
		
		/**
		 * Returns the name of the given object and its short name in parenthesis, if available.
		 * The keys of the name and the short name can be customized, but default to 'name' and 'shortName',
		 * respectively.
		 */
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