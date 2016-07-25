define([
	'lib/joint',
	'shapes/BaseMixin',
	'Util'
], function (
	joint,
	BaseMixin,
	Util
) {
	return Util.set(joint.shapes, 'precise.BaseShape', joint.shapes.basic.Generic.extend(BaseMixin));
});