define([
	'lib/lodash',
	'lib/traverson-hal',
	'lib/halfred'
], function (
	_,
	JsonHalAdapter,
	halfred
) {
	
	ApiService.$inject = ['$q', 'traverson']
	
	function ApiService($q, traverson) {
		// register the traverson-hal plug-in for media type 'application/hal+json'
		traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);
		
		this.baseUrl = baseUrl;
		this.urlTo = urlTo;
		this.from = from;
		this.fromBase = fromBase;
		this.continueFrom = continueFrom;
		this.resultOf = resultOf;
		this.mapReason = mapReason;
		this.extractErrorMessage = extractErrorMessage;
		
		var baseUrl = '/api';
		
		function from(url) {
			return new RootResource(url);
		}
		
		function urlTo(res) {
			return halfred.parse(res).link('self').href;
		}
		
		function fromBase() {
			return new RootResource(baseUrl);
		}
		
		function continueFrom(res) {
			return new RootResource(urlTo(res));
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
			return errReason && (
				errReason.error ? getErrorMessage(errReason.error)
				: errReason.errors ? errReason.errors.map(getErrorMessage).join('. ')
				: getErrorMessage(errReason)
			) || 'Error';
		}
		
		function resolveSuccess(response) {
			var data = response.body;
			if (data && typeof data === 'string')
				data = JSON.parse(data);
			return isSuccess(response.statusCode) ? toResource(data) : $q.reject(data);
		}
		
		function toResource(data) {
			return Array.isArray(data)
				? data.map(halfred.parse)
				: halfred.parse(data);
		}
		
		var getErrorMessage = _.property('message'),
			getResponseData = _.property('data');
		
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
		
	}
	
	return ApiService;
	
});