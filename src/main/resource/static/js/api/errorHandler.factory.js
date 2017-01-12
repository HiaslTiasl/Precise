define([
	'util/util'
], function (
	util
) {
	'use strict';
	
	errorHandlerFactory.$inject = ['$log', '$uibModal', 'toastr', 'PreciseApi'];
	
	function errorHandlerFactory($log, $uibModal, toastr, PreciseApi) {
		
		var ErrorHandler = util.defineClass({
			
			constructor: function (fn) {
				this.handle = fn;
			},
			
			wrapIf: function (errFilter, errProvider) {
				var that = this,
					providedError = typeof errProvider !== 'function' ? errProvider : null;
				return new ErrorHandler(function (error) {
					return that.handle(util.satisfies(errFilter, error)
						? PreciseApi.wrapError(providedError || errProvider(error))
						: error
					);
				});
			}
		
		});
		
		var svc = new ErrorHandler(defaultHandle);
		
		svc.dialog = new ErrorHandler(openDialog);
		svc.toastr = new ErrorHandler(openToastr);
		
		function defaultHandle(error) {
			return Array.isArray(error.data)
				? openDialog(error)
				: openToastr(error);
		}
		
		function openDialog(error) {
			$log.error(error);
			return $uibModal.open({
				component: 'preciseErrorDialog',
				resolve: { errors: error }
			});
		}
		
		function openToastr(error) {
			$log.error(error);
			var data = error.data;
			return toastr.error(
				Array.isArray(data)
					? error.data.map(PreciseApi.getErrorText).join('\n\n')
					: PreciseApi.getErrorText(data),
				data && data.title
			);
		}
		
		return svc;
	}
	
	return errorHandlerFactory;
	
});