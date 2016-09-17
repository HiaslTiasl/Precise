require.config({
	baseUrl: 'js',
	// Paths to named modules, where 'lib/<module>' does not work
	paths: {	
		'jquery': './lib/jquery',
		'svg-pan-zoom': './lib/svg-pan-zoom'
		//'lib/lodash': './lib/lodash.min'	// ... for production use
    },
    map: {
    	// Tell libraries where their dependencies are
        '*': {
        	'lodash': 'lib/lodash',
        	'underscore': 'lib/lodash',		// Use lodash instead of underscore
        	'backbone': 'lib/backbone'
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
    	'lib/angular-ui-router': ['lib/angular'],
    	'lib/ui-bootstrap': ['lib/angular'],
    	'lib/traverson-angular': ['lib/angular'],
    	'lib/smart-table': ['lib/angular'],
    	'lib/ng-file-upload': {
    		deps: ['lib/angular'],
    		exports: 'ngFileUpload'
    	},
    	'lib/xeditable': ['lib/angular'],
//    	'lib/select': ['lib/angular'],
    	'lib/json-formatter': ['lib/angular']
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
	

	// Init split pane
//	var $mainSplitPane = $('#page-main');
//	$mainSplitPane.splitPane();
//	
//	AllModels.$el.show();
	
	// Keep paper dimensions equal to parent
//	var updateDiagramDimensions = dia.updateDimensions.bind(dia);
//	$mainSplitPane.on('splitpaneresize', updateDiagramDimensions);
//	$(window).on('resize', updateDiagramDimensions);
	
	// Sample code for initial data
	// TODO: fetch from server instead
	
//	var scaffInstall = new TaskShape({
//        position: { x: 100, y: 30 },
//        data: {
//        	id: 1,
//        	workers: 4,
//        	timeUnits: 4,
//        	craft: 'Sc',
//        	name: 'Scaffolding Installation'
//        }
//    });
//	
//	var concrPour = new TaskShape({
//        position: { x: 400, y: 30 },
//        data: {
//        	id: 1,
//        	workers: 4,
//        	timeUnits: 4,
//        	craft: 'Br',
//        	name: 'Concrete Pouring'
//        }
//    });
//	
//	var cus = [];
//	for (var unit = 1; unit <= 4; unit++) {
//		cus.push(new ConstructionUnitShape({
//			data: {
//				sector: 'A',
//				level: 'u1',
//				section: 'r',
//				unit: unit
//			}
//		}));
//	}
//	
//	var prec = new Precedence({
//		source: { id: scaffInstall.id },
//		target: { id: concrPour.id },
//		data: {
//			kind: 'CHAIN_PRECEDENCE',
//			scope: 'UNIT'
//		}
//	});
//	
//	var altPrec = new Precedence({
//		source: { id: scaffInstall.id },
//		target: { id: concrPour.id },
//		vertices: [{ x: 350, y: 250 }],
//		data: {
//			kind: 'ALTERNATE_PRECEDENCE',
//			scope: 'LEVEL'
//		}
//	});
//
//	dia.graph.addCells([scaffInstall, concrPour, prec, altPrec]);
//	dia.graph.addCells(cus);
//
//	scaffInstall.embed(cus[0]);
//	scaffInstall.embed(cus[1]);
//	concrPour.embed(cus[2]);
//	concrPour.embed(cus[3]);
});