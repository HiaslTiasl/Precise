/**
 * Angular service for dealing with MDL files.
 * @module "api/MDLFiles.service"
 */
define([
	'util/util'
], function (
	util
) {
	'use strict';
	
	/** Represents MDL files under a given base path. */
	var MDLContext = util.defineClass({
		
		/**
		 * Creates a new MDLContext under the given base path.
		 * @constructor
		 */
		constructor: function (basePath) {
			this.basePath = basePath;
		},
		
		/** Returns the URL for the given model or model name. */
		getModelUrl: function (model) {
			return this.getFileUrl(this.getFileName(model));
		},
		
		/** Returns the URL for the given file name. */
		getFileUrl: function (fileName) {
			return this.basePath + fileName;
		},
		
		/** Returns the file name of the given model or model name. */
		getFileName: function (model) {
			var name = typeof model === 'object' ? model.name : model;
			return name + '.mdl';
		}
	
	});
	
	MDLFilesService.$inject = ['$http', 'Upload', 'PreciseApi'];
	
	/** Service factory. */
	function MDLFilesService($http, Upload, PreciseApi) {
		
		var svc = this;
		
		var basePath = 'files/',						// Base path of MDL files
			configPath = basePath + 'config/',			// Base path of MDL config files
			diagramPath = basePath + 'diagram/';		// Base path of MDL diagram files
		
		svc.base = new MDLContext(basePath);			// Base context for MDL files
		svc.config = new MDLContext(configPath);		// Context for MDL config files
		svc.diagram = new MDLContext(diagramPath);		// Context for MDL diagram files

		svc.importJSON = importJSON;
		svc.duplicate = duplicate;
		svc.clearConfig = clearConfig;
		
		/** Default headers to be sent along with the files. */
		var headers = {
			'Content-Type': 'application/json',
			'Accept': 'application/json'
		};
		
		/** Clears the configuration of the given model. */
		function clearConfig(model) {
			return $http({
				url: this.config.getModelUrl(model),
				method: 'DELETE',
			}).then(PreciseApi.getResponseData);		// Angular's $http returns the complete response
		}
		
		/** Imports the given JSON data into the specified URI. */
		function importJSON(uri, json) {
			return $http({
				url: uri,
				method: 'PUT',
				data: json,
				headers: headers,
			}).then(PreciseApi.getResponseData);		// Angular's $http returns the complete response
		}
		
		/** Creates a copy of the model of the given name under the specified URI. */
		function duplicate(uri, srcName) {
			return $http({
				url: uri,
				method: 'PUT',
				headers: headers,
				params: { use: srcName }
			});
		}
		
	}
	
	return MDLFilesService;
	
});