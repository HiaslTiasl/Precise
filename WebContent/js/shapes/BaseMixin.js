define([
	'lib/joint'
], function (
	joint
) {
	return {
		
		defaults: joint.util.deepSupplement({
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
	    
		data: null
		
	};
});