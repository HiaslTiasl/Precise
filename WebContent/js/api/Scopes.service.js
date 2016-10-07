define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	ScopesService.$inject = ['PreciseApi'];

	function ScopesService(PreciseApi) {
		
		var Types = {
			NONE      : { name: 'NONE'      , displayName: 'None'      , canHaveAttrs: false },
			GLOBAL    : { name: 'GLOBAL'    , displayName: 'Global'    , canHaveAttrs: false },
			ATTRIBUTES: { name: 'ATTRIBUTES', displayName: 'Attributes', canHaveAttrs: true }
		};
		
		this.Types = Types;
		
		this.rereferenceAttributes = rereferenceAttributes;
		this.toLocalRepresentation = toLocalRepresentation;
		this.fromLocalRepresentation = fromLocalRepresentation;
		this.toRequestRepresentation = toRequestRepresentation;
		
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
				attributes: scope.type.canHaveAttrs
					? _.filter(attributes, function (a) {
						return scope.attributes[a.name];
					})
					: undefined 
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
		
	}
	
	return ScopesService;

});