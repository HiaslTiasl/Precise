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
	        this.on('change:data', this.update, this);
	        this.update();

	        joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
	    },
	    
		data: null
		
	};
});