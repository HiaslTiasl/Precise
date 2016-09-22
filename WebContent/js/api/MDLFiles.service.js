define([], function () {
	'use strict';
	
	MDLFilesService.$inject = ['$http', 'Upload', 'PreciseApi', 'SingleModel']
	
	function MDLFilesService($http, Upload, PreciseApi, SIngleMOdel) {
		
		var svc = this;
		
		svc.fileNameOf = fileNameOf;
		svc.urlToModel = urlToModel;
		svc.importJSON = importJSON;
		svc.clearConfig = clearConfig;
		
		var basePath = 'files/',
			configPath = '/config';
		
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
				headers: {
					'Content-Type': 'application/json',
					'Accept': 'application/json'
				}
			}).then(PreciseApi.getResponseData);
		}
		
	}
	
	return MDLFilesService;
	
});