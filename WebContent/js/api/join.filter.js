define(function () {
	
	function joinFilterFactory() {
		
		function joinFilter(arr, sep) {
			return arr && arr.join(sep !== undefined ? sep : ', ');
		}
		
		return joinFilter;

	}
	
	return joinFilterFactory;
	
});