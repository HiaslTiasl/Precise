define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	OrderSpecifications.$inject = ['PreciseApi'];

	function OrderSpecifications(PreciseApi) {
		
		var Types = {
			NONE      : { name: 'NONE'      , displayName: 'None'            , requiresOrdered: false },
			PARALLEL  : { name: 'PARALLEL'  , displayName: '|Parallel|'      , requiresOrdered: false },
			ASCENDING : { name: 'ASCENDING' , displayName: 'Ascending\u2191' , requiresOrdered: true },
			DESCENDING: { name: 'DESCENDING', displayName: 'Descending\u2193', requiresOrdered: true }
		};
		
		this.Types = Types;
		
		this.isAssignableTo = isAssignableTo;
		this.rereferenceAttributes = rereferenceAttributes;
		this.toLocalRepresentation = toLocalRepresentation;
		this.fromLocalRepresentation = fromLocalRepresentation;
		this.toRequestRepresentation = toRequestRepresentation;
		
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
		
		function rereferenceAttributes(orderSpecs, attributes) {
			var attrsByName = _.indexBy(attributes, 'name');
			orderSpecs.forEach(function (os) {
				os.attribute = attrsByName[os.attribute.name];
			});
		};
		
		function toLocalRepresentation(orderSpecs) {
			return {
				attrs: _.chain(orderSpecs)
					.map('attribute')
					.transform(function (acc, attr) {
						acc[attr.name] = true;
					}, {})
					.value(),
				specs: _.map(orderSpecs, function (os) {
					return {
						orderType: Types[os.orderType],
						attribute: os.attribute
					};
				})
			};
		}
		
		function fromLocalRepresentation(order, attributes) {
			return _.map(order.specs, function (os) {
				return {
					orderType: os.orderType.name,
					attribute: os.attribute 
				};
			});
		}
		
		function toRequestRepresentation(orderSpecs) {
			return _.map(orderSpecs, function (os) {
				return {
					orderType: os.orderType,
					attribute: PreciseApi.hrefTo(os.attribute)
				};
			});
		}
		
	}
	
	return OrderSpecifications;

});