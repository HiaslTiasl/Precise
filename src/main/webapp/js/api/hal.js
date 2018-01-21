/**
 * Functions for dealing with the JSON HAL format.
 * @module "api/hal"
 */
define([
	'lib/lodash',
	'lib/url-template'
], function (
	_,
	urlTemplate
) {
	'use strict';
	
	/**
	 * Looks up the link of the given relation and index in obj.
	 * If the relation is not specified, 'self' is assumed.
	 * If the index is not specified, 0 is assumed.
	 * The index only plays a role if multiple links of the given relation are found.
	 */
	function linkTo(obj, rel, index) {
		var link = _.get(obj, ['_links', rel || 'self']);
		return Array.isArray(link) ? link[index || 0] : link;
	}
	
	/**
	 * Looks up the link of the given relation and index in obj,
	 * and returns its href.
	 * @see linkTo
	 */
	function hrefTo(obj, rel, index) {
		var link = obj && linkTo(obj, rel, index);
		return link && link.href;
	}
	
	/** Expands the given url template using the specified params. */
	function resolve(url, params) {
		return url && urlTemplate.parse(url).expand(params || {});
	}
	
	/** Returns the array embedded under the specified relation. */
	function embeddedArray(obj, rel) {
		return _.get(obj, ['_embedded', rel]) || [];
	}
	
	/** Returns the links of the given object. */
	function pickLinks(obj) {
		return _.pick(obj, '_links');
	}
	
	return {
		linkTo: linkTo,
		hrefTo: hrefTo,
		embeddedArray: embeddedArray,
		resolve: resolve,
		pickLinks: pickLinks
	};
	
});