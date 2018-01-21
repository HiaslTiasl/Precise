/**
 * Angular service for error handling.
 * @module "api/errorHandler.factory"
 */
define([
	'util/util'
], function (
	util
) {
	'use strict';
	
	errorHandlerFactory.$inject = ['$log', '$uibModal', 'toastr', 'PreciseApi'];
	
	function errorHandlerFactory($log, $uibModal, toastr, PreciseApi) {
		
		/** Encapsulates an error handler. */
		var ErrorHandler = util.defineClass({
			
			/**
			 * Creates a new error handler using the given handle method.
			 * @constructor
			 */
			constructor: function (fn) {
				this.handle = fn;
			},
			
			/**
			 * Returns a new error handler that delegates to this handler.
			 * An error that matches the given filter will be wrapped using the given
			 * error provider, using the original error as its cause.
			 * Otherwise, the original error is forwarded as-is.
			 */
			wrapIf: function (errFilter, errProvider) {
				var that = this,
					providedError = typeof errProvider !== 'function' ? errProvider : null;
				return new ErrorHandler(function (error) {
					return that.handle(util.satisfies(errFilter, error)
						? PreciseApi.wrapError(providedError || errProvider(error), error)
						: error
					);
				});
			}
		
		});
		
		var svc = new ErrorHandler(defaultHandle);		// Default error handler, and the service itself
		
		svc.dialog = new ErrorHandler(openDialog);		// Error handler that always shows errors in dialogs
		svc.toastr = new ErrorHandler(openToastr);		// Error handler that always shows errors in toasts
		
		/**
		 * Default error handling method.
		 * Shows the error in a dialog if it consists of multiple errors,
		 * in a toast otherwise.
		 */
		function defaultHandle(error) {
			return Array.isArray(error && error.data)
				? openDialog(error)
				: openToastr(error);
		}
		
		/** Shows the error in an modal dialog. */
		function openDialog(error) {
			$log.error(error);
			return $uibModal.open({
				component: 'errorDialog',
				resolve: { error: error }
			});
		}
		
		/** Shows the error in an modal toast. */
		function openToastr(error) {
			$log.error(error);
			return toastr.error(
				PreciseApi.getErrorText(error),
				_.get(error, ['data', 'title'])
			);
		}
		
		return svc;
	}
	
	return errorHandlerFactory;
	
});