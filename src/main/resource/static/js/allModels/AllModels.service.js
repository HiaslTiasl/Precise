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
		svc.deleteModel = deleteModel;
		svc.duplicateModel = duplicateModel;
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
				.then(function (json) {
					return MDLFiles.importJSON(MDLFiles.urlToFile(file.name), json)
				})
				.then(clearCache, PreciseApi.mapReason(PreciseApi.wrapError));
		}
		
		function deleteModel(model) {
			return Models
				.existingResource(model)
				.delete()
				.then(clearCache);
		}
		
		function duplicateModel(model) {
			return MDLFiles.duplicate(MDLFiles.urlToModel(model.name + ' - copy'), model.name)
		}
	}
	
	return AllModelsService;
	
});