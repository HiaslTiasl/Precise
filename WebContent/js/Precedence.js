define(['lodash', 'joint', 'BaseMixin'], function (_, joint, BaseMixin) {
	
	var ALT_PREC_MARKER_SOURCE = 'M 25 5 l -10 -10 M 25 -5 l -10 10';
	
	var scopeLabels = {
		'TASK': 't',
		'SECTOR': 'sr',
		'LEVEL': 'l',
		'SECTION': 'sn',
		'UNIT': 'u'
	};
	
	return joint.dia.Link.extend(BaseMixin).extend({
		defaults: joint.util.deepSupplement({
			type: 'precise.Precedence',
			attrs: {
				'.marker-target': {
					fill: 'black',
					d: 'M 16 6 L 0 0 L 16 -6 Z'
				},
				'.connection': {
					fill: 'black'
				}
			},
			router: {
				name: 'manhattan'
			}
		}, joint.dia.Link.prototype.defaults),
		
		update: function () {
			var data = this.get('data') || {},
				labels = [{
					position: 0.5,
					attrs: { text: { text: scopeLabels[data.scope] || '' } }
				}];
			if (data.kind === 'ALTERNATE_PRECEDENCE') {
				labels.push({
					position: 0.1,
					attrs: { text: { text: 'x' } }
				});
			}
			this.set('labels', labels);
		},
	});
});