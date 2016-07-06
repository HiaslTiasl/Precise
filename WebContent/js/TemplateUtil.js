define(function () {
	
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
	
	function openTag(name, attrs) {
		return toTag(name, attrs, true, false);
	}
	
	function emptyTag(name, attrs) {
		return toTag(name, attrs, true, true);
	}
	
	function closeTag(name) {
		return toTag(name, attrs, false, true);
	}
	
	function createElements(elem, classes) {
		return classes.map(function (c) {
			return emptyTag(elem, {
				'class': c
			});
		});
	}
	
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
		emptyTag: emptyTag,
		closeTag: closeTag,
		createElements: createElements,
		withRefsToSameClass: withRefsToSameClass
	}
});