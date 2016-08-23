define([
	'lib/angular',
	'diagram/diagram',
	'./singleModel.service',
	'./singleModel.controller'
], function (
	angular,
	preciseDiagram,
	SinlgeModelService,
	SingleModelController
) {
	'use strict';
	
	return angular.module('precise.singleModel', [preciseDiagram.name])
		.service('singleModel', SinlgeModelService)
		.controller('SingleModelController', SingleModelController);
	
});