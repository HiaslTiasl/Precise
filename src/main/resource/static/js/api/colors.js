define([
	'lib/tinycolor'
] ,function (
	tinycolor
) {
	'use strict';

	function wrap(method) {
		return function (arg) {
			return method.call(tinycolor(arg));
		};
	}
	
	return {
		fromCSS: wrap(tinycolor.prototype.toRgb),
		toCSS: wrap(tinycolor.prototype.toRgbString),
		toRgb: wrap(tinycolor.prototype.toRgbString),
		toHex: wrap(tinycolor.prototype.toHex)
	};
	
});