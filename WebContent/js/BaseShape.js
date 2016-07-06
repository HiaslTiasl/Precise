define(['joint'], function (joint) {
	return joint.shapes.basic.Generic.extend({
		
		defaults: joint.util.deepSupplement({
			type: 'precise.BaseShape',
			attrs: {
				'rect': { 'stroke': 'black', 'stroke-width': 2 }
			}
		}, joint.shapes.basic.Generic.prototype.defaults),
		
		initialize: function() {

	        this.on('change:data', function() {
	            this.update();
	            this.trigger('precise-update');
	        }, this);

	        this.update();

	        joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
	    },
	    
	    isAllowedAction: function (name) {
	    	return true;
	    },
	    
		data: null
	});
});