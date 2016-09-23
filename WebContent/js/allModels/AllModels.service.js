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
			// TODO: ------------------------- Discuss ----------------------------
			// Here we read the file content as plain text and let the server do the parsing.
			// This is the cleaner approach as the server decides what is valid, plus we only
			// parse the JSON once. 
			// However, if we already parse it before sending, we can avoid sending the file
			// at the first place. Additionally, parsing on the client is also done when importing
			// the building configuration, so we would get the same kind of error messages
			// (Unless we change that, too).
			return Files.newReader()
				.readAsText(file)
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