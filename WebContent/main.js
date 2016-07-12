require.config({
	baseUrl: 'js',
	paths: {
		joint: './lib/joint',
        jquery: './lib/jquery.min',   
        lodash: './lib/lodash.min',
        backbone: './lib/backbone'
    },
    map: {
        '*': {
            // Backbone requires underscore. This forces requireJS to load lodash instead:
            'underscore': 'lodash'
        }
    }
});

require(['lodash', 'joint', 'TaskShape', 'ConstructionUnitShape', 'Precedence'], function (_, joint, TaskShape, ConstructionUnitShape, Precedence) {
	var graph = new joint.dia.Graph(),
		paperWrapper = $('#paper'),
    	paper = new joint.dia.Paper({
	        el: paperWrapper,
	        width: paperWrapper.width(),
	        height: paperWrapper.height(),
	        model: graph,
	        gridSize: 1,
//	        interactive: function (cell, actionName) {
//	        	return cell.model.get('type') !== 'precise.ConstructionUnitShape';
//	        }
	    });
	
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
        position: { x: 100, y: 30 },
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
			kind: 'PRECEDENCE',
			scope: 'UNIT'
		}
	});
	
	var altPrec = new Precedence({
		source: { id: scaffInstall.id },
		target: { id: concrPour.id },
		data: {
			kind: 'ALTERNATE_PRECEDENCE',
			scope: 'LEVEL'
		}
	});

	graph.addCells([scaffInstall, concrPour, prec, altPrec]);
	graph.addCells(cus);

	scaffInstall.embed(cus[0]);
	scaffInstall.embed(cus[1]);
	concrPour.embed(cus[2]);
	concrPour.embed(cus[3]);
	
	
	
	// Test that shows how view reacts to changes in the model
//	setTimeout(function () {
//		var oldData = scaffInstall.get('data'),
//			newData = _.clone(oldData);
//		newData.name = 'XXXXXXXXX';
//		scaffInstall.set('data', newData);
//	}, 2000);
	
//	paper.scaleContentToFit({
//		preserveAspectRatio: true,
//	});

});