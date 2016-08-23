define([
], function () {
	'use strict';
	
	AllModelsService.$inject = ['$http', '$q', 'Upload', 'preciseApi'];
	
	function AllModelsService($http, $q, Upload, preciseApi) {
		
		this.getModels = function () {
			return preciseApi.fromBase().traverse(function (builder) {
				return builder.follow('models', 'models[$all]').get();
			});
		};
		
		this.importFile = function (file) {
			return $q(function (resolve, reject) {
				var reader = new FileReader();
				reader.readAsArrayBuffer(file);
				reader.onloadend = function () {
					if (reader.error)
						reject(reader.error);
					else
						resolve(reader.result);
				};
			}).then(function (data) {
				return $http({
					url: '/files/' + file.name,
					method: 'PUT',
					data: new Uint8Array(data),
					headers: {
						'Content-Type': 'application/json'
					},
					transformRequest: []
				});
			});
		};
		
		this.renameModel = function (model, newName) {
			return preciseApi.continueFrom(model)
				.traverse(function (builder) {
					return builder.patch({ name: newName });
				}).then(function () {
					model.name = newName;
				});
		};
	}
	
	return AllModelsService;
	
});