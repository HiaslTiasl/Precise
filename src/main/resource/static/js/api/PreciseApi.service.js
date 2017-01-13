define([
	'lib/lodash',
	'lib/traverson-hal',
	'api/hal',
	'util/util'
], function (
	_,
	JsonHalAdapter,
	HAL,
	util
) {
	'use strict';
	
	ApiService.$inject = ['$window', '$timeout', '$q', 'traverson'];
	
	function ApiService($window, $timeout, $q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		var getResponseData = _.property('data'),
			isHttpSuccess     = httpStatusChecker(200, 300),
			isHttpClientError = httpStatusChecker(400, 500),
			isHttpBadRequest  = httpStatusChecker(400),
			isHttpNotFound    = httpStatusChecker(404),
			isHttpConflict    = httpStatusChecker(409);
		
		this.basePath = basePath;
		this.linkTo = HAL.linkTo;
		this.hrefTo = HAL.hrefTo;
		this.embeddedArray = HAL.embeddedArray;
		this.from = from;
		this.fromBase = _.once(fromBase);
		this.continueFrom = continueFrom;
		this.resultOf = resultOf;
		this.mapReason = mapReason;
		this.getResponseData = getResponseData;
		this.wrapError = wrapError;
		this.getErrorText = getErrorText;
		this.isHttpClientError = isHttpClientError;
		this.isHttpBadRequest = isHttpBadRequest;
		this.isHttpNotFound = isHttpNotFound;
		this.isHttpConflict = isHttpConflict;
		this.deleteResource = deleteResource;
		
		this.asyncAlert = wrapAsync($window.alert, $window);
		this.asyncConfirm = wrapAsync($window.confirm, $window, _.identity);
		
		var basePath = 'api';
		
		function httpStatusChecker(min, max) {
			return function (response) {
				var status = getHttpStatus(response);
				return max ? _.inRange(status, min, max) : status === min;
			};
		}
		
		function from(url) {
			return new Request(url);
		}
		
		function fromBase() {
			return new Request(basePath);
		}
		
		function continueFrom(res) {
			return new Request(HAL.hrefTo(res));
		}
		
		function resultOf(request) {
			return request.result.then(resolveSuccess, wrapError);
		}
		
		function mapReason(mapper) {
			return function (res) {
				return $q.reject(mapper(res));
			};
		}
		
		function httpErrorMessage(response) {
			return [
				response.config.method,
				response.config.url,
				response.status === -1 ? 'aborted' : response.status,
				response.statusText
			].join(' ');
		}
		
		function isHttpResponse(obj) {
			return util.hasProps(obj, 'status', 'statusText');
		}
		
		function getHttpResponse(obj) {
			return obj && (isHttpResponse(obj) ? obj : obj.httpResponse);
		}
		
		function getHttpStatus(obj) {
			return _.get(getHttpResponse(obj), 'status');
		}
		
		function wrapErrorData(data) {
			return isErrorData(data) ? data : { message: String(data || 'Error') };
		}
		
		function wrapError(val, reason) {
			// Do not re-wrap
			if (isWrappedError(val))
				return val;
			
			var http = isHttpResponse(val);
			
			return {
				data: wrapErrorData(http ? val.data || httpErrorMessage(val) : val),
				reason: (http && val) || reason,
				httpResponse: (http && getHttpResponse(val)) || getHttpResponse(reason)
			};
		}
		
		function isError(obj) {
			return !isHttpResponse(obj) && util.hasProps(obj, 'message');
		}
		
		function isErrorData(data) {
			return Array.isArray(data) ? data.every(isError) : isError(data);
		}
		
		function isWrappedError(obj) {
			return obj && isErrorData(obj.data);
		}
		
		function getErrorText(reason) {
			var errData = isErrorData(reason) ? reason : wrapError(reason).data;
			return errData && (Array.isArray(errData) ? _(errData).map('message') : errData.message);
		}
		
		function resolveSuccess(response) {
			if (typeof response === 'string')	// getUrl was called -> just forward the resulting URL
				return response;
			var data = response.body;
			if (data && typeof data === 'string') {
				// Only consider JSON content to avoid showing HTML code as error message
				var parsed = _.attempt(JSON.parse, data);
				data = response.body = response.data = _.isError(parsed) ? null : parsed;
			}
			return isHttpSuccess(response) ? data : $q.reject(wrapError(response));
		}
		
		function Request(url) {
			this.url = url;
		}
		
		util.defineClass({
			
			constructor: Request,
			
			traverse: function (callback) {
				return resultOf(callback(createTraverson(this.url)));
			},
			
			followAndGet: function () {
				var args = arguments;
				return this.traverse(function (builder) {
					return builder.follow.apply(builder, args).get();
				});
			}
			
		});
		
		function createTraverson(url) {
			return traverson.from(url)
				.jsonHal()
				.useAngularHttp()
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