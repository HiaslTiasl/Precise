define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	ScopesService.$inject = ['PreciseApi'];

	function ScopesService(PreciseApi) {
		
		var Types = {
			UNIT      : { name: 'UNIT'      , displayName: 'Unit' },
			GLOBAL    : { name: 'GLOBAL'    , displayName: 'Global' },
			ATTRIBUTES: { name: 'ATTRIBUTES', displayName: 'Attributes' }
		};
		
		this.Types = Types;
		
		this.rereferenceAttributes = rereferenceAttributes;
		this.toLocalRepresentation = toLocalRepresentation;
		this.fromLocalRepresentation = fromLocalRepresentation;
		this.toRequestRepresentation = toRequestRepresentation;
		this.updateType = updateType;
		this.updateAttributes = updateAttributes;
		
		function rereferenceAttributes(scope, attributes) {
			var scopeLen = _.size(scope.attributes),
				attrLen = attributes.length;
			for (var scopeIdx = 0, attrIdx = 0; scopeIdx < scopeLen; scopeIdx++) {
				var name = scope.attributes[scopeIdx].name;
				while (attrIdx < attrLen && attributes[attrIdx].name !== name)
					attrIdx++;
				scope.attributes[scopeIdx] = attributes[attrIdx];
			}
		};
		
		function toLocalRepresentation(scope) {
			return  {
				type: Types[scope.type],
				attributes: _.transform(scope.attributes, function (res, attr) {
					res[attr.name] = true;
				}, {})
			};
		}
		
		function fromLocalRepresentation(scope, attributes) {
			return {
				type: scope.type.name,
				attributes: _.filter(attributes, function (a) {
					return scope.attributes[a.name];
				}) 
			};
		}
		
		function toRequestRepresentation(scope) {
			return {
				type: scope.type,
				attributes: _.map(scope.attributes, function (attr) {
					return PreciseApi.hrefTo(attr);
				})
			};
		}
		
		function updateType(localScope, allAttrs) {
			var attrCount = _.chain(localScope.attributes).filter().size().value(),
				totalAttrCount = allAttrs.length;
			if (attrCount === totalAttrCount)
				localScope.type = Types.UNIT;
			else if (attrCount > 0 && attrCount < totalAttrCount)
				localScope.type = Types.ATTRIBUTES;
			else if (attrCount == 0 && localScope.type === Types.ATTRIBUTES)
				localScope.type = Types.GLOBAL;
		}
		
		function updateAttributes(localScope, allAttrs) {
			if (localScope.type !== Types.ATTRIBUTES) {
				var value = localScope.type === Types.UNIT;
				_.forEach(allAttrs, function (attr) {
					localScope.attributes[attr.name] = value;
				});			
			}
		}
		
	}
	
	return ScopesService;

});