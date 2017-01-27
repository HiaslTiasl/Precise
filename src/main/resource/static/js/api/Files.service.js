/**
 * Angular service for reading files.
 * @module "api/Files.service"
 */
define([], function () {
	'use strict';
	
	FilesService.$inject = ['$q']
	
	/**
	 * Service constructor.
	 * @constructor
	 */
	function FilesService($q) {
		var svc = this;
		
		svc.newReader = newReader;
		
		/** Creates a new reader. */
		function newReader() {
			return new Reader();
		}
		
		/**
		 * A reader that wraps a FileReader whose methods return
		 * @constructor
		 */
		function Reader() {
			this.fileReader = new FileReader();
		}
		
		Reader.prototype.readAsArrayBuffer  = promisify(FileReader.prototype.readAsArrayBuffer);
		Reader.prototype.readAsBinaryString = promisify(FileReader.prototype.readAsBinaryString);
		Reader.prototype.readAsDataURL      = promisify(FileReader.prototype.readAsDataURL);
		Reader.prototype.readAsText         = promisify(FileReader.prototype.readAsText);
		
		/**
		 * Wrap the given FileReader method in a promise-based method.
		 * The returned promise settles on the 'loadend' event.
		 * Then, the promise is rejected with the error set in the underlying FileReader if available,
		 * otherwise it is resolved with the result. 
		 */
		function promisify(readMethod) {
			return function (file) {
				var r = this.fileReader;
				return $q(function (resolve, reject) {
					r.onloadend = function () {
						if (r.error)
							reject(r.error);
						else
							resolve(r.result);
					};
					readMethod.call(r, file);
				});
			}
		};
		
	};
	
	return FilesService;

});