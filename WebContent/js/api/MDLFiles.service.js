define([], function () {
	'use strict';
	
	MDLFilesService.$inject = ['$http', 'Upload', 'PreciseApi', 'SingleModel']
	
	function MDLFilesService($http, Upload, PreciseApi, SIngleMOdel) {
		
		var svc = this;
		
		svc.fileNameOf = fileNameOf;
		svc.urlToModel = urlToModel;
		svc.urlToFile = urlToFile;
		svc.importJSON = importJSON;
		svc.duplicate = duplicate;
		svc.clearConfig = clearConfig;
		
		var basePath = 'files/',
			configPath = '/config';
		
		var headers = {
			'Content-Type': 'application/json',
			'Accept': 'application/json'
		};
		
		function fileNameOf(model) {
			var name = typeof model === 'object' ? model.name : model;
			return name + '.mdl';
		}
		
		function appendConfigPath(fileURL) {
			return fileURL + configPath;
		}
		
		function urlToFile(fileName, config) {
			var url = basePath + fileName;
			return config ? appendConfigPath(url) : url;
		}
		
		function urlToModel(model, config) {
			return urlToFile(fileNameOf(model), config);
		}
		
		function clearConfig(model) {
			return $http({
				url: urlToModel(model, true),
				method: 'DELETE',
			}).then(PreciseApi.getResponseData);
		}
		
		function importJSON(uri, json) {
			return $http({
				url: uri,
				method: 'PUT',
				data: json,
				headers: headers,
			}).then(PreciseApi.getResponseData);
		}
		
		function duplicate(uri, srcName) {
			return $http({
				url: uri,
				method: 'PUT',
				headers: headers,
				params: { use: srcName }
			})
		}
		
	}
	
	return MDLFilesService;
	
});