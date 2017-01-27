/**
 * Angular service for basic functionalities for communicating with the backend.
 * @module "api/PreciseApi.service"
 */
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
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function ApiService($window, $timeout, $q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		var getResponseData = _.property('data'),				// Extract the body of the response
			isHttpSuccess     = httpStatusChecker(200, 300),	// Does the response status indicate a success?
			isHttpClientError = httpStatusChecker(400, 500),	// Does the response status indicate a client error?
			isHttpBadRequest  = httpStatusChecker(400),			// Does the response status equal BAD_REQUEST?
			isHttpNotFound    = httpStatusChecker(404),			// Does the response status equal NOT_FOUND?
			isHttpConflict    = httpStatusChecker(409);			// Dpes the response status equal CONFLICT?
		
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
		
		/**
		 * Creates a function that indicates whether a given response has a status
		 * in the range between min and max.
		 * If max is omitted, the status must equal min in order to pass the check.
		 */
		function httpStatusChecker(min, max) {
			return function (response) {
				var status = getHttpStatus(response);
				return max ? _.inRange(status, min, max) : status === min;
			};
		}
		
		/**
		 * Returns an error message describing the HTTP error indicated by the given response.
		 * The returned message has the form <method> <url> <statusCode> <statusText>, e.g.:
		 * "GET api/models/1 404 NOT FOUND"
		 */
		function httpErrorMessage(response) {
			return [
				response.config.method,
				response.config.url,
				response.status === -1 ? 'aborted' : response.status,
				response.statusText
			].join(' ');
		}
		
		/** Indicates whether the given object is a HTTP response. */
		function isHttpResponse(obj) {
			return util.hasProps(obj, 'status', 'statusText');
		}
		
		/**
		 * Returns the HTTP response of the given object.
		 * If obj is a HTTP response itself, it is returned unchanged.
		 * If obj is an wrapped error, the corresponding .httpResponse is returned.
		 */
		function getHttpResponse(obj) {
			return obj && (isHttpResponse(obj) ? obj : obj.httpResponse);
		}
		
		/** Returns the HTTP status code of a HTTP response or a wrapped error. */
		function getHttpStatus(obj) {
			return _.get(getHttpResponse(obj), 'status');
		}
		
		/** Wraps the given data in an error object. */
		function wrapErrorData(data) {
			return (data && (										// data must be truthy
					isErrorData(data) && data)						// use it if already error data
					|| (isErrorData(data.errors) && data.errors		// use data.errors if that is error data
				)) || { message: String(data || 'Error') };			// interpret it as string, 'Error' by default
		}
		
		/** Wraps the given value in an error, using specified reason if available. */
		function wrapError(val, reason) {
			// Do not re-wrap
			if (isWrappedError(val))
				return val;
			
			var http = isHttpResponse(val);
			
			return {
				data: wrapErrorData(http ? val.data || httpErrorMessage(val) : val),		// Error description (message, possibly title)
				reason: (http && val) || reason,											// Another error or HTTP response, similar to Exception#cause in Java.
				httpResponse: (http && getHttpResponse(val)) || getHttpResponse(reason)		// The HTTP response that caused the error.
			};
		}
		
		/** Indicates whether obj can be used as an error object. */
		function isError(obj) {
			return util.hasProps(obj, 'message');
		}
		
		/** Indicates whether data can be used as error data. */
		function isErrorData(data) {
			return Array.isArray(data) ? data.every(isError) : isError(data);
		}
		
		/** Indicates whether obj is an error as returned by wrapError. */
		function isWrappedError(obj) {
			return obj && !isHttpResponse(obj) && isErrorData(obj.data);
		}
		
		/** Extracts a single error message from the given error.*/
		function getErrorText(reason) {
			var errData = isErrorData(reason) ? reason : wrapError(reason).data;
			return errData && (Array.isArray(errData) ? _(errData).map('message').join('\n\n') : errData.message);
		}
		
		/** Start a new request from the given URL. */
		function from(url) {
			return new Request(url);
		}
		
		/** Start a new request from the base URL. */
		function fromBase() {
			return new Request(basePath);
		}
		
		/** Start a new request from the self-link of the given data. */
		function continueFrom(res) {
			return new Request(HAL.hrefTo(res));
		}
		
		/**
		 * Returns a promise of the given traverson request.
		 * Unlike request.result, the returned promise is rejected if the HTTP status code
		 * indicates an error.
		 * Any rejection reason is wrapped using wrapError.
		 * @see resolveSuccess
		 * @see wrapError
		 */
		function resultOf(request) {
			return request.result.then(resolveSuccess, wrapError);
		}
		
		/**
		 * Handles a successful response from a traverson request.
		 * Rejects the response in a wrapped error if it has an error status.
		 * Otherwise returns the response body parsed as JSON.
		 */
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
		
		/**
		 * Wraps mapper in a function that rejects its result.
		 * Useful for creating failure handlers of promises.
		 */
		function mapReason(mapper) {
			return function (res) {
				return $q.reject(mapper(res));
			};
		}
		
		/** Creates an instance of traverson of the given URL. */
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
		
		/** Delete the given resource data, based on its self link. */
		function deleteResource(data) {
			return continueFrom(data)
				.traverse(function (builder) {
					return builder.del();
				});
		}
		
		/**
		 * Wraps a traverson instance to use promise behavior as usual in Angular
		 * @constructor
		 */
		function Request(url) {
			this.url = url;
		}
		
		util.defineClass({
			
			constructor: Request,
			
			/**
			 * Creates a traverson instance from the current URL and passes
			 * it to the callback, which uses it to traverse to a desired resource
			 * and calls a HTTP method on it. 
			 * Returns a promise that is rejected for HTTP errors, and that is
			 * resolved with the parsed response body otherwise.
			 * This enables to use the full potential of traverson in the callback
			 * while having a central point for transforming the resulting promise.
			 * An alternative would have been to sub-class traverson, but that would
			 * require to override all HTTP methods. 
			 */
			traverse: function (callback) {
				return resultOf(callback(createTraverson(this.url)));
			},
			
			/** Shortcut method for traversing to a resource and performing a GET without template parameters. */
			followAndGet: function () {
				var args = arguments;
				return this.traverse(function (builder) {
					return builder.follow.apply(builder, args).get();
				});
			}
			
		});

		
		// Async dialogs
		
		/**
		 * Wraps a native dialog function such as confirm, alert, or prompt in an async function.
		 * If provided, the function is invoked with thisArg.
		 * An optional successFilter indicates that promise should be rejected iff it returns a truthy
		 * value when called with the result of the native function.
		 */
		function wrapAsync(nativeFn, thisArg, successFilter) {
			return function () {
				var args = arguments;
				return $q(function (resolve, reject) {
					$timeout(function () {
						var result = nativeFn.apply(thisArg, args);
						if (!successFilter || successFilter(result))
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