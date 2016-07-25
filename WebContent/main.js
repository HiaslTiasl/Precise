require.config({
	baseUrl: 'js',
	paths: {	// library paths
		'jquery': './lib/jquery.min',		// jquery is a named module, so 'lib/jquery' does not work...
		'lib/lodash': './lib/lodash.min'
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
    	'lib/split-pane': ['jquery']
    }
});

require([
	'lib/lodash',
	'jquery',
	'ctrl/Diagram',
	'ctrl/Toolset',
	'ctrl/Toolbar',
	'shapes/TaskShape',
	'shapes/ConstructionUnitShape',
	'shapes/Precedence',
	'lib/split-pane'
], function (
	_,
	$,
	Diagram,
	Toolset,
	Toolbar,
	TaskShape,
	ConstructionUnitShape,
	Precedence
) {
	"use strict";
	
	var dia = new Diagram();
	dia.initPaper('#paper');
	
	Toolbar.initHtmlToolbar('#toolbar', Toolset, dia);
	
	// Fire 'task:select' with null, undefined for initializing listeners
	dia.selectTask(null);

	// Init split pane
	var $mainSplitPane = $('#main');
	$mainSplitPane.splitPane();
	
	// Keep paper dimensions equal to parent
//	var updateDiagramDimensions = dia.updateDimensions.bind(dia);
//	$mainSplitPane.on('splitpaneresize', updateDiagramDimensions);
//	$(window).on('resize', updateDiagramDimensions);
	
	// Sample code for initial data
	// TODO: fetch from server instead
	
	var scaffInstall = new TaskShape({
        position: { x: 100, y: 30 },
        data: {
        	id: 1,
        	workers: 4,
        	timeUnits: 4,
        	craft: 'Sc',
        	name: 'Scaffolding Installation'
        }
    });
	
	var concrPour = new TaskShape({
        position: { x: 400, y: 30 },
        data: {
        	id: 1,
        	workers: 4,
        	timeUnits: 4,
        	craft: 'Br',
        	name: 'Concrete Pouring'
        }
    });
	
	var cus = [];
	for (var unit = 1; unit <= 4; unit++) {
		cus.push(new ConstructionUnitShape({
			data: {
				sector: 'A',
				level: 'u1',
				section: 'r',
				unit: unit
			}
		}));
	}
	
	var prec = new Precedence({
		source: { id: scaffInstall.id },
		target: { id: concrPour.id },
		data: {
			kind: 'CHAIN_PRECEDENCE',
			scope: 'UNIT'
		}
	});
	
	var altPrec = new Precedence({
		source: { id: scaffInstall.id },
		target: { id: concrPour.id },
		vertices: [{ x: 350, y: 250 }],
		data: {
			kind: 'ALTERNATE_PRECEDENCE',
			scope: 'LEVEL'
		}
	});

	dia.graph.addCells([scaffInstall, concrPour, prec, altPrec]);
	dia.graph.addCells(cus);

	scaffInstall.embed(cus[0]);
	scaffInstall.embed(cus[1]);
	concrPour.embed(cus[2]);
	concrPour.embed(cus[3]);
});