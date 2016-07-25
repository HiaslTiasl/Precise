define([
	'lib/lodash'
], function (
	_
) {
	
	function nativeCompare(a, b) {
		return a < b;
	}
	
	return {
		set: function (obj, path, value) {
			var arr = typeof path === "string" ? path.split('.') : path,
				last = arr.length - 1,
				cur = obj;
			for (var i = 0; i < last; i++) {
				var key = arr[i];
				cur = cur[key] || (cur[key] = {});
			}
			return cur[arr[last]] = value;
		},
		
		/** Adapted from java.util.Arrays.binarySearch */
		binarySearch: function (arr, fromIndex, toIndex, key, compare) {
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
	}
});