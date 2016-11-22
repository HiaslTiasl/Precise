define([
	'lib/lodash',
	'util/util'
], function (
	_,
	util
) {
	'use strict';
	
	PagesService.$inject = ['$q', 'PreciseApi'];
	
	function PagesService($q, PreciseApi) {
		
		var Pages = this;
		
		Pages.collectRemaining = collectRemaining;
		Pages.wrap = wrap;
		Pages.wrapper = wrapper;
		Pages.Wrapper = PageWrapper;
		
		var NoSuchPageError = util.defineClass(Error, {
			constructor: function () {}
		});
		
		var NEXT = 'next',
			PREV = 'prev';
		
		function collectRemaining(page, templateParams) {
			return page instanceof PageWrapper
				? page.collectRemaining(templateParams)
				: page;
		}
		
		function collectRemainingImpl(page, templateParams, result) {
			result = result.then(function (resultItems) {
				return page.items().then(function (items) {
					Array.prototype.push.apply(resultItems, items);
					return resultItems;
				});
			});
			return !page.hasNext() ? result : page.next(templateParams).then(function (nextPage) {
				return collectRemainingImpl(nextPage, templateParams, result);
			});
		}
		
		function wrap(rel, data) {
			return new PageWrapper(rel, data);
		}
		
		function wrapper(rel) {
			return function (data) {
				return wrap(rel, data);
			}
		}
		
		function PageWrapper(rel, data) {
			this.rel = rel;
			this.data = data;
		}
		
		util.defineClass({
			
			constructor: PageWrapper,
			
			items: function () {
				return $q.when(PreciseApi.embeddedArray(this.data, this.rel));
			},
			
			hrefTo: function (rel) {
				return PreciseApi.hrefTo(this.data, rel);
			},
			
			has: function (rel) {
				return !!this.hrefTo(rel);
			},
			
			hasNext: function () {
				return this.has(NEXT);
			},
			
			hasPrev: function () {
				return this.has(PREV);
			},
			
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
			
			next: function (templateParams) {
				return this.navigate(NEXT, templateParams);
			},
			
			prev: function (templateParams) {
				return this.navigate(PREV, templateParams);
			},
			
			collectRemaining: function (templateParams) {
				return collectRemainingImpl(this, templateParams, $q.when([]));
			}
			
		});

	}
	
	return PagesService;
	
});