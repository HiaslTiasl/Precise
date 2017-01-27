/**
 * Angular service for dealing with order specifications in resources.
 * @module "api/OrderSpecifications.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	OrderSpecifications.$inject = ['PreciseApi'];

	/**
	 * Service constructor.
	 * @constructor
	 */
	function OrderSpecifications(PreciseApi) {
		
		/** Order types. */
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
		
		/** Indicates whether the given order type is assignable to the given attribute. */
		function isAssignableTo(orderType, attribute) {
			return !orderType.requiresOrdered || attribute.ordered;
		}
		
		/**
		 * Replace attribute objects in orderSpecs with those in the given list of attributes,
		 * matching them by name.
		 */
		function rereferenceAttributes(orderSpecs, attributes) {
			var attrsByName = _.indexBy(attributes, 'name');
			orderSpecs.forEach(function (os) {
				os.attribute = attrsByName[os.attribute.name];
			});
		}
		
		/**
		 * Collect a set of attribute names contained in the given order specs.
		 * An optional destination set of attributes can be specified.
		 * By default, a new set is created.
		 */
		function collectAttrs(orderSpecs, dst) {
			return _.chain(orderSpecs)
				.map('attribute')
				.transform(function (acc, attr) {
					acc[attr.name] = true;
				}, dst || Object.create(null))		// Do not use Object.prototype
				.value();
		}
		
		/**
		 * Converts the given order specs from response to local representation.
		 * In addition to a list of attributes, the local representation maintains
		 * a set of attributes in use, and uses objects with additional information
		 * for order types instead of string constants only.
		 */
		function toLocalRepresentation(orderSpecs) {
			return new LocalSpec({
				attrs: collectAttrs(orderSpecs),
				specs: _.map(orderSpecs, function (os) {
					return {
						orderType: Types[os.orderType],
						attribute: os.attribute
					};
				})
			});
		}
		
		/** Converts the local representation to the response representation. */
		function fromLocalRepresentation(order, attributes) {
			return _.map(order.specs, function (os) {
				return {
					orderType: os.orderType.name,
					attribute: os.attribute 
				};
			});
		}
		
		/** Converts the response representation to the request representation, replacing attributes with links. */
		function toRequestRepresentation(orderSpecs) {
			return _.map(orderSpecs, function (os) {
				return {
					orderType: os.orderType,
					attribute: PreciseApi.hrefTo(os.attribute)
				};
			});
		}
		
		/** Represents an ordering in the local representation, providing manipulation methods. */
		function LocalSpec(props) {
			this.attrs = props.attrs;
			this.specs = props.specs;
		}
		
		util.defineClass({
			
			constructor: LocalSpec,
			
			/** Indicates whether there is an entry for the given attribute. */
			hasAttribute: function (a) {
				return this.attrs[a.name];
			},
			
			/** Adds a new entry with some attribute and order type if possible. */
			addDefaultEntry: function (allAttrs) {
				// Use the first possible attribute
				var attr = _.find(allAttrs, _.negate(_.bind(this.hasAttribute, this)));
				if (attr) {
					this.specs.push({
						orderType: Types.NONE,
						attribute: attr
					});
					this.attrs[attr.name] = true;
				}
			},
			
			/** Swaps the entry at the given index with the previous one. */
			moveToPrev: function (index) {
				util.swap(this.specs, index, index - 1);
			},
			
			/** Swaps the entry at the given index with the next one. */
			moveToNext: function (index) {
				util.swap(this.specs, index, index + 1);
			},
			
			/** Removes the entry at the given index. */
			removeEntry: function (index) {
				var removed = this.specs.splice(index, 1);
				this.attrs[removed[0].attribute.name] = false;
			},
			
			/** Indicates whether another entry can be added. */
			canAddEntry: function (allAttrs) {
				return _.size(this.specs) < _.size(allAttrs);
			},
			
			/** Indicates whether the entry at the given index can be moved to the previous one. */
			canMoveToPrev: function (index) {
				return index > 0;						
			},
			
			/** Indicates whether the entry at the given index can be moved to the next one. */
			canMoveToNext: function (index) {
				return index < _.size(this.specs) - 1;			
			},
			
			/** Updates the set of attributes in use. */
			check: function (order) {
				// Reset attributes map
				_.forEach(order.attrs, function (value, key, attrs) {
					attrs[key] = false;
				});
				// Set attributes contained in specs
				collectAttrs(order.specs, order.attrs);
			}
		});
		
	}
	
	return OrderSpecifications;

});