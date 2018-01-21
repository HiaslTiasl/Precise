/**
 * Utility functions for working with JointJS templates.
 * @module "shapes/TemplateUtils"
 */
define([
	'lib/lodash'
],function (
	_
) {
	'use strict';
	
	/**
	 * Returns a string representing a SVG tag with the given attributes, if any.
	 * The flags open and close can be used to control whether to produce an opening,
	 * a closing, or an empty tag.
	 */
	function toTag(name, attrs, open, close) {
		var res = '<';
		if (open) {
			res += name;
			if (attrs) {
				res += ' ' + _.map(attrs, function (value, key) {
					return key + '="' + value + '"';
				}).join(' ');
				if (close)
					res += '/';
			}
		}
		else if (close)
			res += '/' + name;
		res += '>';
		return res;
	}
	
	/** Returns a string representing an opening SVG tag with the given attributes, if any. */
	function openTag(name, attrs) {
		return toTag(name, attrs, true, false);
	}
	
	/** Returns a string representing a closing SVG tag with the given attributes, if any. */
	function closeTag(name) {
		return toTag(name, attrs, false, true);
	}
	
	/** Returns a string representing an empty SVG tag with the given attributes, if any. */
	function emptyTag(name, attrs) {
		return toTag(name, attrs, true, true);
	}
	
	/**
	 * Returns an array of strings representing SVG attributes, one for each given class, 
	 * and all using the given tag.
	 */
	function createElements(elem, classes) {
		return classes.map(function (c) {
			return emptyTag(elem, {
				'class': c
			});
		});
	}
	
	/**
	 * Returns an object containing JointJS attributes such that
	 * elements with a fromElem tag reference an element of the toElem tag
	 * of the same class for all given classes.
	 * The given attributes are also added to all such mappings.
	 * Useful to link text nodes to corresponding rect nodes.
	 * @example
	 * 	// The following two definitions are equivalent
	 * 	var attrs1 = withReftsToSameClass(
	 * 		'text', 'rect', ['first_name', 'last-name'], { width: '100%' }
	 * 	);
	 * 	var attrs2 = {
	 * 		'text.first-name': {
	 * 			ref: 'rect.first-name',
	 * 			width: '100%'
	 * 		},
	 * 		'text.last-name': {
	 * 			ref: 'rect.last-name',
	 * 			width: '100%' 
	 * 		}
	 * 	}
	 * 	 
	 */
	function withRefsToSameClass(fromElem, toElem, classes, attrs) {
		return _.transform(classes, function (res, c) {
			res[fromElem + '.' + c] = _.assign({
				'ref': toElem + '.' + c
			}, attrs);
		}, {});
	}
	
	return {
		toTag: toTag,
		openTag: openTag,
		closeTag: closeTag,
		emptyTag: emptyTag,
		createElements: createElements,
		withRefsToSameClass: withRefsToSameClass
	};
});