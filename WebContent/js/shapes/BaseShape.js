define([
	'lib/joint',
	'shapes/BaseMixin',
	'util/util'
], function (
	joint,
	BaseMixin,
	util
) {
	return util.set(joint.shapes, 'precise.BaseShape', joint.shapes.basic.Generic.extend(BaseMixin));
});