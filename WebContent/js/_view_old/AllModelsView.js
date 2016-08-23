define([
	'jquery',
	'lib/lodash',
	'lib/joint',
	'view/TableView',
	'view/DiagramView'
], function (
	$,
	_,
	joint,
	TableView
) {
	'use strict';
	
	return TableView.extend({
		initialize: function () {
			TableView.prototype.initialize({
				title: 'All Models',
				el: '#all-models',
				tableToolbar: {
					buttons: [ {
						key: 'create',
					}, {
						key: 'import'
					} ]
				},
				rowToolbar: {
					title: 'Actions',
					buttons: [ {
						key: 'open',
					}, function (data) {
						var fileName = data.name + '.mdl';
						return {
							key: 'export',
							tagName: 'a',
							attrs: {
								href: 'files/' + fileName,
								//download: fileName,
								target: '_blank'
							}
						}
					}, {
						key: 'rename',
					}, {
						key: 'duplicate',
					}, {
						key: 'delete' 
					} ],
					events: {
						'click .open': function () {
							this.$el.hide();
							var diaView = new DiagramView();
							diaView.$el.show();
						},
						'click .export': function () {
							
						},
						'click .rename': function () {
							
						},
						'click .delete': function () {
							
						}
					}
				},
				bootstrap: {
					url: '/api/models',
					responseHandler: _.property(['_embedded', 'models']),
					showHeader: true,
					showColumns: true,
					showRefresh: true,
					search: true,
					columns: [ {
						title: 'Name',
						field: 'name',
						switchable: false,
						searchable: true
					}, {
						title: 'Description',
						field: 'description',
						switchable: true,
						searchable: false
					} ]
				}
			});
		}

	});
});