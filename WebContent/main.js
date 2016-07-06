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

require(['joint', 'TaskShape', 'ConstructionUnitShape'], function (joint, TaskShape, ConstructionUnitShape) {
	var graph = new joint.dia.Graph(),
		paperWrapper = $('#paper'),
    	paper = new joint.dia.Paper({
	        el: paperWrapper,
	        width: paperWrapper.width(),
	        height: paperWrapper.height(),
	        model: graph,
	        gridSize: 1,
	        interactive: function (cell, actionName) {
	        	return cell.model.isAllowedAction(actionName);
	        }
	    });
	
	var task = new TaskShape({
        position: { x: 100, y: 30 },
        data: {
        	id: 1,
        	workers: 4,
        	timeUnits: 4,
        	craft: 'Sc',
        	name: 'Scaffolding Installation'
        }
    });
	
	var cu1 = new ConstructionUnitShape({
		data: {
			sector: 'A',
			level: 'u1',
			section: 'r',
			unit: 1,
		}
	}), cu2 = new ConstructionUnitShape({
		data: {
			sector: 'A',
			level: 'u1',
			section: 'r',
			unit: 2,
		}
	});
	
	graph.addCells([task, cu1, cu2]);

	task.embed(cu1);
	task.embed(cu2);
	
//	paper.scaleContentToFit({
//		preserveAspectRatio: true,
//	});

});