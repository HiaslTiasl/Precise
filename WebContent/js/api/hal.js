define([
	'lib/lodash',
	'lib/url-template'
], function (
	_,
	urlTemplate
) {
	'use strict';
	
	function linkTo(obj, rel, index) {
		var r = rel || 'self',
			link = obj && obj._links && obj._links[r];
		return Array.isArray(link) ? link[index || 0] : link;
	}
	
	function hrefTo(obj, rel, index) {
		var link = obj && linkTo(obj, rel, index);
		return link && link.href;
	}
	
	function resolve(url, params) {
		return urlTemplate.parse(url).expand(params || {});
	}
	
	function embeddedArray(obj, rel) {
		return _.get(obj, ['_embedded', rel]);
	}
	
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