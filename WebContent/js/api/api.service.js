define([
	'lib/lodash',
	'lib/traverson-hal'
], function (
	_,
	JsonHalAdapter
) {
	
	ApiService.$inject = ['$window', '$timeout', '$q', 'traverson']
	
	function ApiService($window, $timeout, $q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		var getErrorMessage = _.property('message'),
			getResponseData = _.property('data');
		
		this.baseUrl = baseUrl;
		this.linkTo = linkTo;
		this.hrefTo = hrefTo;
		this.from = from;
		this.fromBase = fromBase;
		this.continueFrom = continueFrom;
		this.resultOf = resultOf;
		this.mapReason = mapReason;
		this.extractErrorMessage = extractErrorMessage;
		this.getResponseData = getResponseData;
		this.deleteResource = deleteResource;
		
		this.asyncAlert = wrapAsync($window.alert, $window);
		this.asyncConfirm = wrapAsync($window.confirm, $window, _.identity);
		
		var baseUrl = '/api';
		
		function linkTo(obj, rel, index) {
			var r = rel || 'self',
				link = obj && typeof obj.link === 'function'
					? obj.link(r)
					: obj._links && obj._links[r];
			return Array.isArray(link) ? link[index || 0] : link;
		}
		
		function hrefTo(obj, rel, index) {
			var link = obj && linkTo(obj, rel, index);
			return link && link.href;
		}
		
		function from(url) {
			return new RootResource(url);
		}
		
		function fromBase() {
			return new RootResource(baseUrl);
		}
		
		function continueFrom(res) {
			return new RootResource(hrefTo(res));
		}
		
		function resultOf(request) {
			var promise = Array.isArray(request)
			return request.result.then(resolveSuccess, mapReason(getResponseData));
		}
		
		function mapReason(mapper) {
			return function (res) {
				return $q.reject(mapper(res));
			};
		}
		
		// TODO: improve
		function extractErrorMessage(errReason) {
			return errReason
				&& errReason.errors && errReason.errors.map(getErrorMessage).join('. ')		// Multiple Validation errors
				|| getErrorMessage(errReason)												// Exception message 
				|| 'Error';																	// Fallback
		}
		
		function resolveSuccess(response) {
			var data = response.body;
			if (data && typeof data === 'string')
				data = JSON.parse(data);
			return isSuccess(response.statusCode) ? data : $q.reject(data);
		}
		
		/**
		 * https://github.com/angular/angular.js/blob/master/src/ng/http.js#L235
		 */
		function isSuccess(status) {
			return 200 <= status && status < 300;
		}
		
		function RootResource(url) {
			this.url = url;
		}
		
		RootResource.prototype.traverse = function (callback) {
			return resultOf(callback(createTraverson(this.url)));
		};
		
		RootResource.prototype.followAndGet = function () {
			var args = arguments
			return this.traverse(function (builder) {
				return builder.follow.apply(builder, args).get();
			});
		};
		
		function createTraverson(url) {
			return traverson.from(url)
				.jsonHal()
				.withRequestOptions({
					headers: {
						'Content-Type': 'application/json',
						'Accept': 'application/hal+json'
					}
				});
		}
		
		function deleteResource(data) {
			return continueFrom(data)
				.traverse(function (builder) {
					return builder.del();
				});
		}
		
		// Async dialogs
		
		function wrapAsync(nativeFn, thisArg, successFilter) {
			return function () {
				var args = arguments;
				return $q(function (resolve, reject) {
					$timeout(function () {
						var result = nativeFn.apply(thisArg, args);
						if (successFilter && successFilter(result))
							resolve(result);
						else
							reject(result);
					});
				});
			};
		}
		
	}
	
	return ApiService;
	
});