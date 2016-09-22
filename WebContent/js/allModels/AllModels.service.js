define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsService.$inject = ['$q', 'Files', 'MDLFiles', 'PreciseApi', 'Models'];
	
	function AllModelsService($q, Files, MDLFiles, PreciseApi, Models) {
		
		var svc = this;
		
		svc.getModels = getModels;
		svc.importFile = importFile;
		svc.renameModel = renameModel;
		svc.deleteModel = deleteModel;
		svc.cachedModels = null;
		svc.clearCache = clearCache;
		
		function clearCache() {
			svc.cachedModels = null;
		}
		
		function cacheModels(models) {
			return svc.cachedModels = models;
		}
		
		function getModels() {
			return svc.cachedModels ? $q.when(svc.cachedModels) : Models.findAll().then(cacheModels);
		}
		
		function importFile(file) {
			return Files.newReader()
				.readAsText(file)
				.then(JSON.parse)
				.then(function (json) {
					return MDLFiles.importJSON(MDLFiles.urlToFile(file.name), json)
				})
				.then(clearCache, PreciseApi.mapReason(PreciseApi.toErrorMessage));
		}
		
		function renameModel(model, newName) {
			return PreciseApi.continueFrom(model)
				.traverse(function (builder) {
					return builder.patch({ name: newName });
				}).then(function () {
					model.name = newName;
				}).then(clearCache);
		}
		
		function deleteModel(model) {
			return Models
				.existingResource(model)
				.delete()
				.then(clearCache);
		}
	}
	
	return AllModelsService;
	
});