define([
], function () {
	'use strict';
	
	AllModelsService.$inject = ['Upload', 'preciseApi'];
	
	function AllModelsService(Upload, preciseApi) {
		
		this.getModels = getModels;
		this.importFile = importFile;
		this.renameModel = renameModel;
		this.deleteModel = deleteModel;
		
		function getModels() {
			return preciseApi.fromBase().traverse(function (builder) {
				return builder.follow('models', 'models[$all]').get();
			});
		}
		
		function importFile(file) {
			return Upload.http({
				url: '/files/' + file.name,
				method: 'PUT',
				data: file,
				headers: {
					'Content-Type': 'application/json',
					'Accept': 'application/json'
				}
			}).then(
				preciseApi.getResponseData,
				preciseApi.mapReason(preciseApi.getResponseData)
			);
		}
		
		function renameModel(model, newName) {
			return preciseApi.continueFrom(model)
				.traverse(function (builder) {
					return builder.patch({ name: newName });
				}).then(function () {
					model.name = newName;
				});
		}
		
		function deleteModel(model) {
			return preciseApi.continueFrom(model)
				.traverse(function (builder) {
					return builder.del();
				});
		}
	}
	
	return AllModelsService;
	
});