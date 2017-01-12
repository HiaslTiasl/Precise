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
	
	ApiService.$inject = ['$window', '$timeout', '$q', 'traverson'];
	
	function ApiService($window, $timeout, $q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		var getResponseData = _.property('data'),
			getErrorStatus = _.property(['httpResponse', 'status']);
		
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
		this.responseError = responseError;
		this.getError = getError;
		this.wrapError = wrapError;
		this.getErrorText = getErrorText;
		this.isHttpClientError = isHttpClientError;
		this.isHttpConflict = isHttpConflict;
		this.isHttpBadRequest = isHttpBadRequest;
		this.deleteResource = deleteResource;
		
		this.asyncAlert = wrapAsync($window.alert, $window);
		this.asyncConfirm = wrapAsync($window.confirm, $window, _.identity);
		
		var basePath = 'api';
		
		function getErrorMessage(error) {
			return error
				&& (error.property + '=' + error.invalidValue + ': ' + error.message); 
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
			return request.result.then(resolveSuccess, getError);
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
			return 'status' in obj && 'statusText' in obj;
		}
		
		function wrapError(data, reason) {
			return {
				data: typeof data === 'object' ? data : { message: data.toString() },
				reason: reason,
				httpResponse: reason && (isHttpResponse(reason) ? reason : reason.httpResponse)
			};
		}
		
		// TODO: improve
		function responseError(response) {
			return wrapError(response.data || httpErrorMessage(response), response);
		}
		
		function getError(reason) {
			var error;
			switch (typeof reason) {
			case 'string':
				error = wrapError(reason);
				break;
			case 'object':
				if ('message' in reason)
					error = reason;
				else if (isHttpResponse(reason))
					error = responseError(reason);
				break;
			}
			return error || wrapError('Error');
		}
		
		function getErrorText(reason) {
			var err = getError(reason);
			return Array.isArray(err) ? _(err).map('message') : err.message
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
			return isSuccess(response) ? data : $q.reject(getError(response));
		}
		
		function isSuccess(response) {
			return _.inRange(response.statusCode, 200, 300);
		}
		
		function isHttpClientError(error) {
			return _.inRange(getErrorStatus(error), 400, 500);
		}
		
		function isHttpConflict(error) {
			return getErrorStatus(error) === 409;
		}
		
		function isHttpBadRequest(error) {
			return getErrorStatus(error) === 400;
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