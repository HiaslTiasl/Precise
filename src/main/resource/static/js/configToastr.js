define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	configToastr.$inject = ['toastrConfig']
	
	function configToastr(toastrConfig) {
		_.assign(toastrConfig, {
			positionClass: 'toast-bottom-center',
			closeButton: true,
			timeOut: 10000,
			extendedTimeout: 5000
		});
	}
	
	return configToastr;
	
});