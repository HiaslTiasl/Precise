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
			PREV = 'prev',
			ALL_EMBEDDED = '[$all]';
		
		function guessExisting(data) {
			return !!PreciseApi.linkTo(data);
		}
		
		function collectRemaining(page) {
			return page.collectRemaining();
		}
		
		function collectRemainingImpl(page, result) {
			result = result.then(function (resultItems) {
				return page.items().then(function (items) {
					Array.prototype.push.apply(resultItems, items);
					return resultItems;
				});
			});
			return !page.hasNext() ? result : page.next().then(function (nextPage) {
				return collectRemainingImpl(nextPage, result);
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
				var rel = this.rel;
				return PreciseApi.continueFrom(this.data)
					.traverse(function (builder) {
						return builder.follow(rel + ALL_EMBEDDED).get();
					});
			},
			
			hrefTo: function (rel) {
				return PreciseApi.linkTo(this.data, rel);
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
			
			navigate: function (direction) {
				var rel = this.rel,
					href = this.hrefTo(direction);
				return !href ? $q.reject(new NoSuchPageError())
					: PreciseApi
						.continueFrom(href)
						.followAndGet()
						.then(function (page) {
							return new PageWrapper(page, rel);
						});
			},
			
			next: function () {
				return this.navigate(NEXT);
			},
			
			prev: function () {
				return this.navigate(PREV);
			},
			
			collectRemaining: function () {
				return collectRemainingImpl(this, $q.when([]));
			}
			
		});

	}
	
	return PagesService;
	
});