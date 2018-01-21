define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	/**
	 * Sets the given value under the given path of the given object.
	 * The path can either be an array of properties or a string where
	 * '.' is interpreted as property delimiter.
	 * Returns value.
	 */
	function set(obj, path, value) {
		var arr = typeof path === "string" ? path.split('.') : path,
			last = arr.length - 1,
			cur = obj;
		for (var i = 0; i < last; i++) {
			var key = arr[i];
			cur = cur[key] || (cur[key] = {});
		}
		return cur[arr[last]] = value;
	}
	
	/** 
	 * Indicates whether the given object has all properties specified
	 * in the following arguments.
	 */
	function hasProps(obj) {
		var res = false;
		if (obj && typeof obj === 'object') {
			var len = arguments.length;
			res = true;
			for (var i = 1; res && i < len; i++)
				res = arguments[i] in obj;
		}
		return res;
	}
	
	/**
	 * Sets the given prototype object as the prototype property of its
	 * constructor property.
	 * The parent constructor is optional.
	 */
	function defineClass(Parent, proto) {
		if (!proto) {
			proto = Parent;
			Parent = Object;
		}
		var parentProto = typeof Parent === 'function'
			? Parent.prototype
			: Parent;
		proto.constructor.prototype = _.create(parentProto, proto);
		return proto.constructor;
	}
	
	/**
	 * Indicates whether the given value is an integer.
	 * @see https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger#Polyfill
	 */
	function isInteger(value) {
		return typeof value === "number"
			&& isFinite(value) 
			&& Math.floor(value) === value;
	}
	
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/random
	 * Returns a random integer between min (included) and max (included)
	 * using Math.round() will give you a non-uniform distribution!
	 */
	function getRandomIntIncl(min, max) {
		min = Math.ceil(min);
		max = Math.floor(max);
		return Math.floor(Math.random() * (max - min + 1)) + min;
	}
	
	/** Returns true. */
	function getTrue() {
		return true;
	}
	
	/** Indicates whether the given collection is empty. */
	function isEmpty(collection) {
		return _.some(collection, getTrue);
	}
	
	/**
	 * Indicates whether the given value satisfies the given filter.
	 * If filter is a function, it is satisfied iff invoking it with value returns a truthy value.
	 * Otherwise, the filter is satisfied iff it (deeply) equals to value.
	 */
	function satisfies(filter, value) {
		return typeof filter === 'function' ? !!filter(value) : _.isEqual(filter, value);
	}
	
	/** Swaps the values of the two given keys (indices) in the given object (array). */
	function swap(obj, key1, key2) {
		var temp = obj[key1];
		obj[key1] = obj[key2];
		obj[key2] = temp;
	}
	
	return {
		set: set,
		hasProps: hasProps,
		defineClass: defineClass,
		isInteger: isInteger,
		getRandomIntIncl: getRandomIntIncl,
		satisfies: satisfies,
		swap: swap
	};
});