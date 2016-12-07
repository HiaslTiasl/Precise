define(function () {
	'use strict';
	
	run.$inject = ['editableOptions'];
	
	function run(editableOptions) {
		editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
	}
	
	return run;
	
});