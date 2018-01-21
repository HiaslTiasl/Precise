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
	
	MDLFilesService.$inject = ['$http', 'Upload', 'PreciseApi', 'RemoteFiles'];
	
	/** Service factory. */
	function MDLFilesService($http, Upload, PreciseApi, RemoteFiles) {
		
		var svc = this;
		
		var extension = '.mdl';
		
		svc.base    = RemoteFiles.context({ extension: extension });						// Base context for MDL files
		svc.config  = RemoteFiles.context({ extension: extension, path: 'config/' });		// Context for MDL config files
		svc.diagram = RemoteFiles.context({ extension: extension, path: 'diagram/' });		// Context for MDL diagram files

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