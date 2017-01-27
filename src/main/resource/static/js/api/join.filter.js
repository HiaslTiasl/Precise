/**
 * Angular filter that joins an array by a given separator
 * @module "api/join.filter"
 */
define(function () {
	'use strict';
	
	/** Creates the filter. */
	function joinFilterFactory() {
		
		/** Joins elements of arr by sep, using ', ' as the default separator. */
		function joinFilter(arr, sep) {
			return arr && arr.join(sep !== undefined ? sep : ', ');
		}
		
		return joinFilter;

	}
	
	return joinFilterFactory;
	
});