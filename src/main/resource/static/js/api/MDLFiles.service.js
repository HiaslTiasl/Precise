define([
	'util/util'
], function (
	util
) {
	'use strict';
	
	mdlFilesFactory.$inject = ['$http', 'Upload', 'PreciseApi', 'SingleModel'];
	
	var MDLContext = util.defineClass({
		
		constructor: function (basePath) {
			this.basePath = basePath;
		},
		
		getModelUrl: function (model) {
			return this.getFileUrl(this.getFileName(model));
		},
		
		getFileUrl: function (file) {
			return this.basePath + file;
		},
		
		getFileName: function (model) {
			var name = typeof model === 'object' ? model.name : model;
			return name + '.mdl';
		}
	
	});
	
	function mdlFilesFactory($http, Upload, PreciseApi, SIngleMOdel) {
		
		var basePath = 'files/',
			configPath = basePath + 'config/',
			diagramPath = basePath + 'diagram/';
		
		var svc = {
			base: new MDLContext(basePath),
			config: new MDLContext(configPath),
			diagram: new MDLContext(diagramPath),
			importJSON: importJSON,
			duplicate: duplicate,
			clearConfig: clearConfig
		};
		
		svc.importJSON = importJSON;
		svc.duplicate = duplicate;
		svc.clearConfig = clearConfig;
		
		
		var headers = {
			'Content-Type': 'application/json',
			'Accept': 'application/json'
		};
		
		function clearConfig(model) {
			return $http({
				url: this.config.getModelUrl(model),
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
		
		return svc;
		
	}
	
	return mdlFilesFactory;
	
});