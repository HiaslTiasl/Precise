/**
 * Angular service for reading files.
 * @module "api/Files.service"
 */
define([
	'util/util'
], function (
	util
) {
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
		 * A reader that wraps a FileReader whose methods return promises.
		 * @constructor
		 */
		var Reader = util.defineClass({
			constructor: function Reader() {
				this.fileReader = new FileReader();
			},
			readAsArrayBuffer  : promisify(FileReader.prototype.readAsArrayBuffer),
			readAsBinaryString : promisify(FileReader.prototype.readAsBinaryString),
		    readAsDataURL      : promisify(FileReader.prototype.readAsDataURL),
			readAsText         : promisify(FileReader.prototype.readAsText)
		}); 
		
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