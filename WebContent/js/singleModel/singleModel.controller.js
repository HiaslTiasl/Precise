define(function () {
	'use strict';
	
	SingleModelController.$inject = ['$scope', 'model'];
	
	function SingleModelController($scope, model) {
		
		var $ctrl = this;
		
		$ctrl.model = model;
		
		$ctrl.createModel = createModel;
		$ctrl.importModel = importModel;
		$ctrl.renameModel = renameModel;
		$ctrl.duplicateModel = duplicateModel;
		$ctrl.deleteModel = deleteModel;
		
		$scope.$on('cell:select', function (event, cellView) {
			$ctrl.selectedCell = cellView;
		});
		
		function createModel(model) {
			
		}
		
		function importModel(model) {
			
		}
		
		function openModel(model) {
			
		}
		
		function renameModel(model) {
			
		}
		
		function duplicateModel(model) {
			
		}
		
		function deleteModel(model) {
			
		}
	}
	
	return SingleModelController;
})
