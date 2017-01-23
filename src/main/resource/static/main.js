require.config({
	baseUrl: 'js',
	// Paths to named modules, where 'lib/<module>' does not work
	paths: {	
		'jquery': './lib/jquery',
		'svg-pan-zoom': './lib/svg-pan-zoom',
		'angular-ui-router': './lib/angular-ui-router',
		'lib/angular-toastr': './lib/angular-toastr.tpls'
		//'lib/lodash': './lib/lodash.min'	// ... for production use
    },
    map: {
    	// Tell libraries where their dependencies are
        '*': {
        	'lodash': 'lib/lodash',
        	'underscore': 'lib/lodash',		// Use lodash instead of underscore
        	'backbone': 'lib/backbone',
        	'tinycolor2': 'lib/tinycolor'
        }
    },
    shim: {
    	'lib/split-pane': ['jquery'],
    	'lib/bootstrap': ['jquery'],
    	'lib/bootstrap-table': ['jquery', 'lib/bootstrap'],
    	'lib/angular': {
    		deps: ['lib/jquery'],
    		exports: 'angular'
    	},
    	'angular-ui-router': ['lib/angular'],
    	'lib/ui-bootstrap': ['lib/angular'],
    	'lib/traverson-angular': ['lib/angular'],
    	'lib/smart-table': ['lib/angular'],
    	'lib/ng-file-upload': {
    		deps: ['lib/angular'],
    		exports: 'ngFileUpload'
    	},
    	'lib/json-formatter': ['lib/angular'],
    	'lib/angular-toastr': ['lib/angular'],
    	'lib/angularjs-color-picker': ['lib/tinycolor', 'lib/angular']
    }
});

require([
	'jquery',
	'lib/angular',
	'app'
], function (
	$,
	angular,
	app
) {
	'use strict';
	
	$(function () {
		angular.bootstrap(document.body, [app.name]);
	});
	
});