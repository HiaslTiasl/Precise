/**
 * Angular service for dealing with paged resources.
 * @module "api/Pages.service"
 */
define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	PagesService.$inject = ['$q', 'PreciseApi'];
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function PagesService($q, PreciseApi) {
		
		var Pages = this;
		
		Pages.collectRemaining = collectRemaining;
		Pages.wrap = wrap;
		Pages.wrapper = wrapper;
		Pages.Wrapper = PageWrapper;
		
		/**
		 * Error indicating that the target page does not exist.
		 * @constructor
		 */
		var NoSuchPageError = util.defineClass(Error, {
			constructor: function () {}
		});
		
		var NEXT = 'next',		// Relation to the next page
			PREV = 'prev';		// Relation to the previous page
		
		/**
		 * Follows the next relation starting from the given page
		 * until the last page and collects all embedded data
		 * into a single array.
		 * If provided, next links are expanded using the given template
		 * parameters.
		 * If page is not a PageWrapper, it is returned unchanged.
		 */
		function collectRemaining(page, templateParams) {
			return page instanceof PageWrapper
				? page.collectRemaining(templateParams)
				: page;
		}
		
		/**
		 * Recursive implementation of PageWrapper#collectRemaining.
		 * Passes a promise of the overall result to recursive calls
		 * in an additional parameter.
		 */
		function collectRemainingImpl(page, templateParams, result) {
			// We use 2 chains of promises: one for the result, and one for the navigation
			// through the pages.
			// The two chains are relatively independent: we do not have to wait for the items
			// of a page before going to the next page.
			// The only important thing is that each page receives the promise of the items until the
			// previous page and creates a new promise that also includes the items of this page.
			// On the last page, the promise of all items is returned, but no assumption must be
			// made about whether it is already resolved.
			result = result.then(function (resultItems) {
				return page.items().then(function (items) {
					// Add all items to resultItems.
					// N.B.: This mutates resultItems and therefore the original promise insofar as further calls
					// to result.then would receive the new list including all items added here. However, it is
					// avoid creating a new array and copying all values, and is safe to do here because:
					// 		a) we redefine result to the new promise
					// 		b) only the last promise leaves this function, i.e. we are in full control
					Array.prototype.push.apply(resultItems, items);
					return resultItems;
				});
			});
			return !page.hasNext() ? result												// Base case: last page
				: page.next(templateParams).then(function (nextPage) {
					return collectRemainingImpl(nextPage, templateParams, result);		// Recursion
				});
		}
		
		/** Wraps the given data of the given relation in a PageWrapper. */
		function wrap(rel, data) {
			return new PageWrapper(rel, data);
		}
		
		/**
		 * Returns a function that wraps data in a PageWrapper using the given relation.
		 * Useful for promise handlers.
		 */
		function wrapper(rel) {
			return function (data) {
				return wrap(rel, data);
			}
		}
		
		/**
		 * Represents data wrapped in a paged resource.
		 * @constructor
		 */
		function PageWrapper(rel, data) {
			this.rel = rel;		// Relation of the embedded data
			this.data = data;	// The embedded data
		}
		
		util.defineClass({
			
			constructor: PageWrapper,
			
			/** Returns a promise to the items embedded in the current page. */
			items: function () {
				// Technically no need to use a promise, all data is available already.
				// Only reason is to guard against future changes.
				return $q.when(PreciseApi.embeddedArray(this.data, this.rel));
			},
			
			/** Returns the href of the link of the given relation. */
			hrefTo: function (rel) {
				return PreciseApi.hrefTo(this.data, rel);
			},
			
			/** Indicates whether the page has a link of the given relation. */
			has: function (rel) {
				return !!this.hrefTo(rel);
			},
			
			/** Indicates whether the page has a next link. */
			hasNext: function () {
				return this.has(NEXT);
			},
			
			/** Indicates whether the page has a prev link. */
			hasPrev: function () {
				return this.has(PREV);
			},
			
			/**
			 * Navigates to a new page using the relation specified by direction.
			 * Returns a promise of the resulting page.
			 * If the link indicated by direction does not exist, the promise is rejected
			 * with a NoSuchPageError.
			 */
			navigate: function (direction, templateParams) {
				var rel = this.rel,
					href = this.hrefTo(direction);
				return !href ? $q.reject(new NoSuchPageError())
					: PreciseApi
						.from(href)
						.traverse(function (builder) {
							return builder.withTemplateParameters(templateParams).get();
						})
						.then(wrapper(rel));
			},
			
			/** Navigates to the next page. */
			next: function (templateParams) {
				return this.navigate(NEXT, templateParams);
			},
			
			/** Navigates to the previous page. */
			prev: function (templateParams) {
				return this.navigate(PREV, templateParams);
			},
			
			/** Collects all the remaining pages, using the given template parameters if available. */
			collectRemaining: function (templateParams) {
				return collectRemainingImpl(this, templateParams, $q.when([]));
			}
			
		});

	}
	
	return PagesService;
	
});