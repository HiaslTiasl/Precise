/**
 * Angular service for dealing with scopes in resources.
 * @module "api/Scopes.service"
 */
define([
	'lib/lodash',
	'api/hal'
], function (
	_,
	HAL
) {
	'use strict';
	
	ScopesService.$inject = [];

	/**
	 * Service constructor.
	 * @constructor
	 */
	function ScopesService() {
		
		/** Scope types. */
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
		
		/**
		 * Replace attribute objects in scope with those in the given list of attributes,
		 * matching them by name.
		 */
		function rereferenceAttributes(scope, attributes) {
			if (scope) {
				var scopeLen = _.size(scope.attributes),
				attrLen = attributes.length;
				for (var scopeIdx = 0, attrIdx = 0; scopeIdx < scopeLen; scopeIdx++) {
					var name = scope.attributes[scopeIdx].name;
					while (attrIdx < attrLen && attributes[attrIdx].name !== name)
						attrIdx++;
					scope.attributes[scopeIdx] = attributes[attrIdx];
				}
			}
		}
		
		/**
		 * Converts the given scope from response to local representation.
		 * Instead of a list of attributes, the local representation maintains a set
		 * of attributes in use, and uses objects with additional information for order
		 * types instead of string constants only.
		 */
		function toLocalRepresentation(scope) {
			return scope && {
				type: Types[scope.type],
				attributes: _.transform(scope.attributes, function (res, attr) {
					res[attr.name] = true;
				}, Object.create(null))		// Do not use Object.prototype
			};
		}
		
		/** Converts the local representation to the response representation. */
		function fromLocalRepresentation(scope, attributes) {
			return scope && {
				type: scope.type.name,
				attributes: _.filter(attributes, function (a) {
					return scope.attributes[a.name];
				}) 
			};
		}
		
		/** Converts the response representation to the request representation, replacing attributes with links. */
		function toRequestRepresentation(scope) {
			return scope && {
				type: scope.type,
				attributes: _.map(scope.attributes, function (attr) {
					return HAL.resolve(HAL.hrefTo(attr));
				})
			};
		}
		
		/**
		 * Updates the scope type of given localScope based on its attributes
		 * and the given list of all allowed attributes.
		 * If all attributes are in use, the UNIT type is set.
		 * Otherwise, if no attributes are in use, the GLOBAL type is set.
		 * Otherwise, the ATTRIBUTES type is set.
		 */
		function updateType(localScope, allAttrs) {
			var attrCount = _.chain(localScope.attributes).filter().size().value(),
				totalAttrCount = allAttrs.length;
			// N.B.: We assume that localScope can only contain attributes in allAttrs,
			// and thus only check the number of attributes in use without matching them. 
			// This should be a safe assumption, and it is not clear what to do if it does not hold.
			if (attrCount === totalAttrCount)
				localScope.type = Types.UNIT;
			else if (attrCount > 0 && attrCount < totalAttrCount)
				localScope.type = Types.ATTRIBUTES;
			else if (attrCount == 0 && localScope.type === Types.ATTRIBUTES)
				localScope.type = Types.GLOBAL;
		}
		
		/**
		 * Updates the attributes of the given localScpe based on its type
		 * and the given list of all allowed attributes.
		 * If the type is UNIT, all allowed attributes are set.
		 * Otherwise, if the type is GLOBAL, all attributes are removed.
		 * Otherwise nothing is done.
		 */
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