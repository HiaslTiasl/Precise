define([], function () {
	'use strict';
	
	MDLFilesService.$inject = ['$http', 'Upload', 'PreciseApi', 'SingleModel']
	
	function MDLFilesService($http, Upload, PreciseApi, SIngleMOdel) {
		
		var svc = this;
		
		svc.getFileName = getFileName;
		svc.getModelFileURI = getModelFileURI;
		svc.getConfigFileURI = getConfigFileURI;
		svc.importModelFile = importModelFile;
		svc.importConfigFile = importConfigFile;
		svc.clearConfig = clearConfig;
		
		var basePath = 'files/',
			configPath = '/config';
		
		function getFileName(model) {
			return model.name + '.mdl';
		}
		
		function getModelFileURI(model) {
			return basePath + getFileName(model);
		}
		
		function getConfigFileURI(model) {
			return getModelFileURI(model) + configPath;
		}
		
		function importModelFile(model, json) {
			return importFile(getModelFileURI(model), json);
		}
		
		function importConfigFile(model, json) {
			return importFile(getConfigFileURI(model), json);
		}
		
		function clearConfig(model) {
			return $http({
				url: getConfigFileURI(model),
				method: 'DELETE',
			}).then(PreciseApi.getResponseData);
		}
		
		function importFile(uri, json) {
			return $http({
				url: uri,
				method: 'PUT',
				data: json,
				headers: {
					'Content-Type': 'application/json',
					'Accept': 'application/json'
				}
			}).then(PreciseApi.getResponseData);
//			return Upload.http({
//				url: uri,
//				method: 'PUT',
//				data: file,
//				headers: {
//					'Content-Type': 'application/json',
//					'Accept': 'application/json'
//				}
//			}).then(preciseApi.getResponseData);
		}
		
	}
	
	return MDLFilesService;
	
});