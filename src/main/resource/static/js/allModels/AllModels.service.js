/**
 * Service for operations on the list of models that involve the backend server.
 * @module "allModels/AllModels.service"
 */
define([
	'lib/lodash'
], function (
	_
) {
	'use strict';
	
	AllModelsService.$inject = ['$q', 'Files', 'RemoteFiles', 'MDLFiles', 'PreciseApi', 'Models'];
	
	/** @constructor */
	function AllModelsService($q, Files, RemoteFiles, MDLFiles, PreciseApi, Models) {
		
		var svc = this;
		
		svc.getModels = getModels;
		svc.importFile = importFile;
		svc.deleteModel = deleteModel;
		svc.duplicateModel = duplicateModel;
		svc.cachedModels = null;
		svc.clearCache = clearCache;
		
		svc.remoteFiles = {
			mdl: MDLFiles,
			csv: RemoteFiles.context({ extension: '.csv' }),
			smv: RemoteFiles.context({ extension: '.smv' }),
			graph: RemoteFiles.context({ path: 'graph/', extension: '.json' }),
			orientation: RemoteFiles.context({ path: 'graph/orientation/', extension: '.json' })
		};
		
		/** Clears cache of models. */
		function clearCache() {
			svc.cachedModels = null;
		}
		
		/** Caches the given list of models. */
		function cacheModels(models) {
			return svc.cachedModels = models;
		}
		
		/** Returns a list of models, if possible from the cache. */
		function getModels() {
			return svc.cachedModels ? $q.when(svc.cachedModels) : Models.findAll().then(cacheModels);
		}
		
		/** Uploads the given file to import it as a new model. */
		function importFile(file) {
			return Files.newReader()
				.readAsText(file)
				.then(function (json) {
					return MDLFiles.importJSON(MDLFiles.base.getFileUrl(file.name), json)
				})
				.then(clearCache, PreciseApi.mapReason(PreciseApi.wrapError));
		}
		
		/** Deletes the given model in the DB. */
		function deleteModel(model) {
			return Models
				.existingResource(model)
				.delete()
				.then(clearCache);
		}
		
		/** Duplicates the given model.*/
		function duplicateModel(model) {
			return MDLFiles.duplicate(MDLFiles.base.getModelUrl(model.name + ' - copy'), model.name)
		}

	}
	
	return AllModelsService;
	
});