define([], function () {
	'use strict';
	
	FilesService.$inject = ['$q']
	
	function FilesService($q) {
		var svc = this;
		
		svc.newReader = newReader;
		
		function newReader() {
			return new Reader();
		}
		
		function Reader() {
			this.fileReader = new FileReader();
		}
		
		Reader.prototype.readAsArrayBuffer  = promisify(FileReader.prototype.readAsArrayBuffer);
		Reader.prototype.readAsBinaryString = promisify(FileReader.prototype.readAsBinaryString);
		Reader.prototype.readAsDataURL      = promisify(FileReader.prototype.readAsDataURL);
		Reader.prototype.readAsText         = promisify(FileReader.prototype.readAsText);
		
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