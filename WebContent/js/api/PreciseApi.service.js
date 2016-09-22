define([
	'lib/lodash',
	'lib/traverson-hal',
	'util/util'
], function (
	_,
	JsonHalAdapter,
	util
) {
	
	ApiService.$inject = ['$window', '$timeout', '$q', 'traverson'];
	
	function ApiService($window, $timeout, $q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		var getErrorMessage = _.property('message'),
			getResponseData = _.property('data');
		
		this.basePath = basePath;
		this.linkTo = linkTo;
		this.hrefTo = hrefTo;
		this.embeddedArray = embeddedArray;
		this.from = from;
		this.fromBase = _.once(fromBase);
		this.continueFrom = continueFrom;
		this.resultOf = resultOf;
		this.mapReason = mapReason;
		this.getResponseData = getResponseData;
		this.responseErrorMessage = responseErrorMessage;
		this.toErrorMessage = toErrorMessage;
		this.deleteResource = deleteResource;
		
		this.asyncAlert = wrapAsync($window.alert, $window);
		this.asyncConfirm = wrapAsync($window.confirm, $window, _.identity);
		
		var basePath = 'api';
		
		function linkTo(obj, rel, index) {
			var r = rel || 'self',
				link = obj && obj._links && obj._links[r];
			return Array.isArray(link) ? link[index || 0] : link;
		}
		
		function hrefTo(obj, rel, index) {
			var link = obj && linkTo(obj, rel, index);
			return link && link.href;
		}
		
		function embeddedArray(obj, rel) {
			return _.get(obj, ['_embedded', rel]);
		}
		
		function from(url) {
			return new Request(url);
		}
		
		function fromBase() {
			return new Request(basePath);
		}
		
		function continueFrom(res) {
			return new Request(hrefTo(res));
		}
		
		function resultOf(request) {
			return request.result.then(resolveSuccess);
		}
		
		function mapReason(mapper) {
			return function (res) {
				return $q.reject(mapper(res));
			};
		}
		
		// TODO: improve
		function responseErrorMessage(response) {
			var data = response.data;
			return !data
				? [response.config.method, response.config.url, response.status, response.statusText].join(' ')		// No error message -> report HTTP failure
				: data.errors && data.errors.map(getErrorMessage).join('. ')										// Multiple Validation errors
					|| getErrorMessage(data)																		// Exception message
					|| data;																						// Something else
		}
		
		function toErrorMessage(reason) {
			var msg;
			switch (typeof reason) {
			case 'string':
				msg = reason;
				break;
			case 'object':
				if (reason instanceof Error)
					msg = reason.message;
				else if ('status' in reason && 'statusText' in reason)
					msg = responseErrorMessage(reason);
				break;
			}
			return msg || 'Error';
		}
		
		function resolveSuccess(response) {
			var data = response.body;
			if (data && typeof data === 'string')
				data = response.body = response.data = JSON.parse(data);
			return isSuccess(response) ? data : $q.reject(response);
		}
		
		function isSuccess(response) {
			return _.inRange(response.statusCode, 200, 300);
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