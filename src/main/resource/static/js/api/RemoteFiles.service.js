/**
 * Angular service for dealing with files on the server of a particular kind,
 * e.g. MDL files, MDL config files, CSV files etc.
 * @module "api/RemoteFiles.service"
 */
define([
	'util/util'
], function (
	util
) {
	'use strict';
	
	/** Represents files under a given base path of a given extension. */
	var FileContext = util.defineClass({
		
		/**
		 * Creates a new FileContext with the given base path and extension.
		 * @constructor
		 */
		constructor: function (basePath, extension) {
			this.basePath = basePath;
			this.extension = extension;
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
			return name + this.extension;
		}
	
	});
	
	/**
	 * Service constructor
	 * @constructor
	 */
	function RemoteFilesService() {
		var svc = this;
		
		svc.context = context;
		
		var basePath = 'files/';
		
		/** Returns the given path relative to the basePath. */
		function underBase(path) {
			return path ? basePath + path : basePath;
		}
		
		/** Returns a FileContext object with the given options. */
		function context(options) {
			return new FileContext(underBase(options.path), options.extension);
		}
		
	}
	
	return RemoteFilesService;
	
});