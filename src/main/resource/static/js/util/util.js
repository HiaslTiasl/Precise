define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	function limitArray(arr, length) {
		while (arr.length > length)
			arr.pop();
	}

	function mapInto(dst, src, mapper, thisArg) {
		for (var i = 0, len = src.length; i < len; i++) {
			var val = src[i];
			dst[i] = thisArg
				? mapper.call(thisArg, val, i, src)
				: mapper(val, i, src);
		}
		limitArray(dst, len);
		return dst;
	}
	
	function strInsert(str, newStr, index) {
		return str.substring(0, index) + newStr + str.substring(index);
	}
	
	function strInsertBefore(str, newStr, subStr) {
		return strInsert(str, newStr, str.indexOf(subStr));
	}
	
	function nativeCompare(a, b) {
		return a < b;
	}
	
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
	
	/** Adapted from java.util.Arrays.binarySearch */
	function binarySearch(arr, fromIndex, toIndex, key, compare) {
		if (typeof compare !== "function")
			compare = nativeCompare;
		var low = fromIndex || 0,
			high = toIndex >= 0 ? toIndex - 1 : arr.length;
		
		while (low <= high) {
			var mid = (low + high) >>> 1,
				midVal = arr[mid],
				cmp = compare(midVal, key);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1);  // key not found.
	}
	
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
	
	// https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger#Polyfill
	function isInteger(value) {
		return typeof value === "number"
			&& isFinite(value) 
			&& Math.floor(value) === value;
	}
	
	function satisfies(filter, value) {
		return typeof filter === 'function' ? filter(value) : _.isEqual(filter, value);
	}
	
	function swap(obj, key1, key2) {
		var temp = obj[key1];
		obj[key1] = obj[key2];
		obj[key2] = temp;
	}
	
	return {
		limitArray: limitArray,
		mapInto: mapInto,
		strInsert: strInsert,
		strInsertBefore: strInsertBefore,
		set: set,
		hasProps: hasProps,
		defineClass: defineClass,
		isInteger: isInteger,
		satisfies: satisfies,
		swap: swap
	};
});