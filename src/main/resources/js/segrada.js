(function ($) {
	/**
	 * tags tokenizer
	 */
	var tagsTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			wildcard: '%QUERY',
			url: urlSegradaTagSearch + '%QUERY'
		}
	});

	/**
	 * node tokenizer
	 */
	var nodeTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			url: urlSegradaNodeSearch,
			replace: function(url, uriEncodedQuery) {
				var searchUrl = url + uriEncodedQuery;

				// get current textField
				var textField = $(".sg-node-search").filter(":focus");
				var relationTypeSelect = $("#" + textField.attr('data-select-id') + ' option').filter(":selected");
				var contraintIds = relationTypeSelect.attr(textField.attr('data-attr'));
				if (contraintIds != null && contraintIds.length > 0) {
					// add list of ids
					searchUrl += '&tags=' + encodeURIComponent(contraintIds);
				}

				return searchUrl;
			}
		}
	});

	/**
	 * file tokenizer
	 */
	var fileTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			wildcard: '%QUERY',
			url: urlSegradaFileSearch + '%QUERY'
		}
	});

	/**
	 * source tokenizer
	 */
	var sourceTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			wildcard: '%QUERY',
			url: urlSegradaSourceSearch + '%QUERY'
		}
	});

	// geocoding object
	var geocoder = (typeof google === 'object' && typeof google.maps === 'object')?new google.maps.Geocoder():null;

	// is graph initialized?
	var graphInitialized = false;
	// global graph data
	var graphNodes = new vis.DataSet([]);
	var graphEdges = new vis.DataSet([]);
	var graphNetwork = null; // reference to graph network

	/**
	 * on enter pressed event
	 * @param func
	 * @returns {jQuery}
	 */
	$.fn.onEnter = function (func) {
		this.bind('keypress', function (e) {
			if (e.keyCode == 13) func.apply(this, [e]);
		});
		return this;
	};

	// will find first id of string (used in ajax)
	var idRegex = new RegExp(/<([^\s]+).*?id="([^"]*?)".*?>/i);

	/**
	 * helper function to create pictogram chooser elements
	 * @param modal containing chooser
	 * @param modalContent content part of modal
	 * @param myId of element
	 * @param part to replace stuff in
	 * @param term to search for
	 */
	function segradaPictogramChooser(modal, modalContent, myId, part, term) {
		var search = urlSegradaPictogramSearch + encodeURIComponent(term);
		$.getJSON(search, function (data) {
			var items = [];
			$.each(data, function (idx, element) {
				var encodedTitle = $('<div/>').text(element.title).html();
				items.push('<div class="col-xs-1 sg-no-padding-right"><a class="sg-pictogram-modal-link" href="#" data-id="' + element.id + '" data-uid="' + element.uid + '" title="' + encodedTitle + '"><img src="' + urlSegradaPictogramFile + element.uid + '" width="24" height="24" alt="' + encodedTitle + '" /></a></div>');
			});
			modalContent.html("<div class='row'>" + items.join("") + "</div>");
			$('a', modalContent).click(function (e2) {
				var picId = $(this).attr('data-id');
				var picUid = $(this).attr('data-uid');
				var picEncodedTitle = $('<div/>').text($(this).attr('title')).html();
				$("#value-" + myId, part).val(picId);
				// add preview
				$("#preview-" + myId, part).html('<img src="' + urlSegradaPictogramFile + picUid + '" width="24" height="24" alt="' + picEncodedTitle + '" /> ' + picEncodedTitle);
				// show hide button
				$("#clear-" + myId, part).show();
				e2.preventDefault();
				modal.modal('hide');
			});
		});
	}

	/**
	 * load url and show in data add area of page
	 * @param url
	 */
	function loadDataAddUrl(url) {
		$.get(url, function (data) {
			// find id and hide duplicate elements
			var matches = data.match(idRegex);
			if (matches!=null&&matches.length >= 2) {
				$('#' + matches[2]).remove();
			}

			var container = $('#sg-data');
			container.prepend(data);

			var addedChild = container.children(":first");
			// call after AJAX event
			afterAjax(addedChild);
			// scroll to element
			$('html, body').animate({
				scrollTop: addedChild.offset().top
			}, 500);
		});
	}

	/**
	 * called after ajax calls to update a specific part of the document
	 * @param part
	 */
	function afterAjax(part) {
		// default
		part = part || $('body');

		// *******************************************************
		// make data elements dynamic
		$(".sg-data").addClass("sg-dynamic-data");

		// show headbox
		$(".sg-headbox-right").show();

		// dynamic hide
		$(".sg-dynamic-hide").hide();

		// *******************************************************
		// data add links - add data at top of data container
		$('.sg-data-add', part).click(function (e) {
			loadDataAddUrl($(this).attr('href'));
			e.preventDefault();
		});

		// *******************************************************
		// bind control form
		$('.sg-control-form', part).ajaxForm({
			beforeSubmit: function(arr, $form, options) {
				// dynamic target?
				var target = $form.attr('data-target-id');
				if (typeof target == "undefined" || target == null || target.length == 0) {
					target = '#sg-control';

					// hide graph
					graphHide();
				}
				var container = $(target);
				// disable container
				container.wrapInner("<div class='sg-disabled'></div>")
				container.prepend($('#sg-wait').html());
				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// dynamic target?
				var target = $form.attr('data-target-id');
				if (typeof target == "undefined" || target == null || target.length == 0) target = '#sg-control';
				var container = $(target);
				container.html(responseText);
				afterAjax(container);
			}
		});

		// *******************************************************
		// add content to control area
		$('.sg-control-set', part).click(function (e) {
			var $this = $(this);

			var target = $this.attr('data-target-id');
			if (typeof target == "undefined" || target == null || target.length == 0) {
				target = '#sg-control';

				// hide graph
				graphHide();
			}

			// define container and set waiting icon
			var container = $(target);
			container.wrapInner("<div class='sg-disabled'></div>")
			container.prepend($('#sg-wait').html());

			// AJAX call
			$.get($this.attr('href'), function (data) {
				container.html(data); // replace html in container
				// call after AJAX event
				afterAjax(container);
			});
			e.preventDefault();
		});

		// *******************************************************
		// double click data handler
		$('[data-data-dblclick]', part).dblclick(function () {
			// AJAX call
			$.get($(this).attr('data-data-dblclick'), function (data) {
				// find id and hide duplicate elements
				var matches = data.match(idRegex);
				if (matches!=null&&matches.length >= 2) {
					$('#' + matches[2]).remove();
				}

				var container = $('#sg-data');
				container.prepend(data);

				var addedChild = container.children(":first");
				// call after AJAX event
				afterAjax(addedChild);
				// scroll to element
				$('html, body').animate({
					scrollTop: addedChild.offset().top
				}, 500);
			});
		});

		// *******************************************************
		// delete confirmation
		$('tr [data-confirm]', part).click(function (e) {
			var $this = $(this);

			if (confirm($this.attr('data-confirm'))) {
				// remove tr dynamically
				var row = $this.closest('tr');
				row.addClass("sg-disabled");

				// AJAX call
				$.get($this.attr('href'), function (data) {
					// delete row
					row.slideUp('fast', function() {
						row.remove();
					});
				});
			}
			e.preventDefault();
		});

		// *******************************************************
		// load tab contents dynamically
		$('.sg-replace-content', part).on('shown.bs.tab', function (e) {
			var $content = $($(this).attr('href'));
			var url = $(this).attr('data-url');

			// load via ajax
			$.get(url, function(content) {
				$content.html(content);
				// call after AJAX event
				afterAjax($content);
			});

			// unbind action
			$(this).removeClass('sg-replace-content');
			$(this).unbind('shown.bs.tab');
		});

		// *******************************************************
		// add data element closer (the one to close all
		// is in the common element area below
		$(".sg-data-close", part).click(function (e) {
			$(this).parent().parent().fadeOut('fast', function () {
				$(this).remove();
			});
		});

		// *******************************************************
		// init file uploads
		$("input.sg-fileupload", part).fileinput({
			'showUpload': false
		});
		// small
		$("input.sg-fileupload-small", part).fileinput({
			'showUpload': false,
			'previewSettings': {
				image: {width: "auto", height: "24px"}
			}
		});

		// *******************************************************
		// pictogram chooser
		$(".sg-pictogram-modal").on('shown.bs.modal', function () {
			var modal = $(this);
			var myId = modal.attr('id');
			var modalContent = $("#container-" + myId, modal);
			var inputField = $("#filter-" + myId, modal);

			// listener for chooser
			inputField.on('input propertychange paste', function () {
				segradaPictogramChooser(modal, modalContent, myId, part, $(this).val());
			}).onEnter(function () { // pressed enter
				// get first image
				var firstImg = $(".sg-pictogram-modal-link", modalContent).first();
				if (firstImg.length > 0) { // if defined, load first image
					var picId = firstImg.attr('data-id');
					var picUid = firstImg.attr('data-id');
					var picEncodedTitle = $('<div/>').text(firstImg.attr('title')).html();
					$("#value-" + myId, part).val(picId);
					// add preview
					$("#preview-" + myId, part).html('<img src="' + urlSegradaPictogramFile + picUid + '" width="24" height="24" alt="' + picEncodedTitle + '" /> ' + picEncodedTitle);
					// show hide button
					$("#clear-" + myId, part).show();
					modal.modal('hide');
				}
			});
			// initial list
			if (inputField.val() === "")
				segradaPictogramChooser(modal, modalContent, myId, part, "");
		});
		$(".sg-pictogram-chooser", part).click(function (e) {
			$('#' + $(this).attr('data-id')).modal('show');
			e.preventDefault();
		});
		$(".sg-pictogram-clearer", part).click(function (e) {
			var myId = $(this).attr('data-id');
			$("#value-" + myId, part).val('');
			$("#preview-" + myId, part).html('');
			$(this).hide();
			e.preventDefault();
		});

		// *******************************************************
		// source ref editor modal
		$(".sg-source-ref-modal").on('shown.bs.modal', function () {
			var modal = $(this);
			var myId = modal.attr('id');
			var modalContent = $(".modal-body", modal);

			$.get(modal.attr('data-href'), function (data) {
				modalContent.html(data);
				$('form', modalContent).ajaxForm({
					beforeSubmit: function(arr, $form, options) {
						// disable container
						$form.wrapInner("<div class='sg-disabled'></div>")
						$form.prepend($('#sg-wait').html());
						return true;
					},
					success: function (responseText, statusText, xhr, $form) {
						// replace target by response text
						var target = $(modal.attr('data-target'));
						target.html(responseText);
						afterAjax(target);
						modal.modal('hide');
					}
				});
			});
		}).on('hidden.bs.modal', function () {
			$(".modal-body", $(this)).html($('#sg-wait').html()); // replace by waiting icon
		});
		// source ref editor
		$(".sg-source-ref-editor", part).click(function (e) {
			var myModal = $('#' + $(this).attr('data-id'));
			myModal.attr("data-href", $(this).attr('href'));
			myModal.modal('show');
			e.preventDefault();
		});

		// *******************************************************
		// contractable tag list
		$('.sg-taglist-contract', part).each(function() {
			var tags = $('span', $(this));
			if (tags.length > 1) {
				tags.hide().filter(":first-child").show().after('<span class="sg-tag-show label label-default"><i class="fa fa-plus"></i></span>');
				$('span.sg-tag-show', $(this)).click(function() {
					$(this).remove();
					tags.show();
				});
			}
		});

		// *******************************************************
		// color picker init
		$("select.sg-colorpicker", part).simplepicker({
			theme: 'fontawesome'
		});

		// *******************************************************
		// Tags fields
		$("select.sg-tags", part).tagsinput({
			trimValue: true,
			confirmKeys: [13], //enter only
			typeaheadjs: {
				name: 'tags',
				displayKey: 'title',
				valueKey: 'title',
				source: tagsTokenizer.ttAdapter()
			}
		});

		// *******************************************************
		// node selector for relation forms
		$("input.sg-node-search", part).each(function() {
			var $this = $(this);
			var target = $('#' + $this.attr('data-id'));

			$this.typeahead({hint: true,
				highlight: true,
				minLength: 1
			},{
				name: 'node',
				displayKey: 'title',
				valueKey: 'id',
				source: nodeTokenizer.ttAdapter()
			}).bind('typeahead:selected', function(e, datum) {
				target.val(datum.id);
			}).bind('keyup', function() { // empty on textbox empty
				if (!this.value) {
					target.val('');
				}
			});
		});

		// *******************************************************
		// node selector for relation forms
		$("input.sg-file-search", part).each(function() {
			var $this = $(this);
			var target = $('#' + $this.attr('data-id'));

			$this.typeahead({hint: true,
				highlight: true,
				minLength: 1
			},{
				name: 'file',
				displayKey: 'title',
				valueKey: 'id',
				source: fileTokenizer.ttAdapter()
			}).bind('typeahead:selected', function(e, datum) {
				target.val(datum.id);
			}).bind('keyup', function() { // empty on textbox empty
				if (!this.value) {
					target.val('');
				}
			});
		});

		// source selector for forms
		$("input.sg-source-search", part).each(function() {
			var $this = $(this);
			var target = $('#' + $this.attr('data-id'));

			$this.typeahead({hint: true,
				highlight: true,
				minLength: 1
			},{
				name: 'source',
				displayKey: 'title',
				valueKey: 'id',
				source: sourceTokenizer.ttAdapter()
			}).bind('typeahead:selected', function(e, datum) {
				target.val(datum.id);
			}).bind('keyup', function() { // empty on textbox empty
				if (!this.value) {
					target.val('');
				}
			});
		});

		// bind external links
		$(".sg-link-external").click(function(e) {
			var url = $(this).attr('href');
			var win = window.open(url, '_blank');
			win.focus();
			e.preventDefault();
		});

		// *******************************************************
		// bind data forms (left side)
		$("form.sg-data-form", part).ajaxForm({
			beforeSubmit: function (arr, $form, options) {
				// disable form elements
				$(":input", $form).attr("disabled", true);

				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// determine target to replace
				var target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.replaceWith(responseText);

				// find id and rerun bindings
				var matches = responseText.match(idRegex);
				if (matches!=null&&matches.length >= 2) {
					afterAjax($('#' + matches[2]));
				}
			}
		});

		// second type of data form (left side)
		$("form.sg-simple-form", part).ajaxForm({
			beforeSubmit: function (arr, $form, options) {
				// disable form elements
				$(":input", $form).attr("disabled", true);

				// determine target to replace
				var target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.html($('#sg-wait'));

				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// determine target to replace
				var target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.html(responseText);

				$(":input", $form).attr("disabled", false);
			}
		});

		// *******************************************************
		// period handler
		$('.sg-periods').each(function() {
			var container = $(this);
			var id = container.attr('id');

			var form = $('.sg-period-form', container);

			// show add form
			$('.sg-period-add', container).click(function(e) {
				$(this).hide();
				form.show();
				e.preventDefault();
			});

			// show/hide period field
			$('.sg-period-form-period', container).change(function(e) {
				if ($(this).is(':checked')) $('.sg-period-toggle', container).show();
				else $('.sg-period-toggle', container).hide();
			});

			// form submit
			form.ajaxForm({
				beforeSubmit: function(arr, $form, options) {
					// disable container
					container.addClass('disabled');
					return true;
				},
				success: function (responseText, statusText, xhr, $form) {
					container.replaceWith(responseText);
					afterAjax(container);
				}
			});
		});

		// *******************************************************
		// dynamic map loader
		$('.sg-geotab', part).on('shown.bs.tab', function (e) {
			var target = $(e.target);

			// map created already?
			if (target.attr('data-created') == '1') return;

			// set flag
			target.attr('data-created', '1');

			var id = target.attr('data-locations-id');
			var container = $(id);

			var form = $('.sg-map-form', container);
			var input = $('.sg-geocomplete', form);
			var point = new google.maps.LatLng(0, 0);
			input.geocomplete({
				map: id + '-map',
				details: id + '-form',
				markerOptions: {
					draggable: true
				},
				mapOptions: {
					mapTypeId: google.maps.MapTypeId.HYBRID,
					zoom: 1,
					center: point,
					scrollwheel: true,
					draggable: true
				}
			}).bind("geocode:result", function(event, result) {
				$('input[type=submit]', form).show();
			}).bind("geocode:dragged", function(event, latLng){
				$("input[name=lat]", form).val(latLng.lat());
				$("input[name=lng]", form).val(latLng.lng());
			});

			// add form adder
			$('.sg-map-add-marker', container).click(function(e) {
				$(this).hide();
				form.show();
				e.preventDefault();
			});

			// add markers
			updateGeocompleteMap(id, $(id + '-markers', container), input);

			// add removal listener to clean marker array below
			$(id + '-map').on("destroyed", function () {
				if(typeof segradaMapMarkers[id] !== 'undefined') {
					// remove markers
					for (var i = 0; i < segradaMapMarkers[id].length; i++)
						segradaMapMarkers[id][i].setMap(null);

					delete segradaMapMarkers[id];
				}

				//console.log(segradaMapMarkers);
			});

			$('.sg-geocomplete-find', form).click(function(){
				input.trigger("geocode");
			}).click(); // click to create map

			// form submitted
			form.ajaxForm({
				beforeSubmit: function(arr, $form, options) {
					// disable container
					container.addClass('disabled');
					return true;
				},
				success: function (responseText, statusText, xhr, $form) {
					container.removeClass('disabled');
					$(id + '-markers', container).replaceWith(responseText);
					updateGeocompleteMap(id, $(id + '-markers', container), input);
				}
			});
		});

		// *******************************************************
		// Graph: load remote data and update graph view
		$('a.sg-graph-update', part).click(function(e) {
			// update graph by remotely getting updated data
			graphLoadRemote($(this).attr('href'));
			e.preventDefault();
		});
	} // afterAJAX end

	// keeps markers
	var segradaMapMarkers = [];

	// update map with markers
	function updateGeocompleteMap(id, markerContainer, input) {
		var map = input.geocomplete("map");

		// clear old markers
		if(typeof segradaMapMarkers[id] !== 'undefined') {
			// remove markers
			for (var i = 0; i < segradaMapMarkers[id].length; i++)
				segradaMapMarkers[id][i].setMap(null);

			delete segradaMapMarkers[id];
		}

		// create new marker array
		segradaMapMarkers[id] = [];
		var deleteText = $(id + '-delete').html();

		// this is the bounding box container
		var bounds = new google.maps.LatLngBounds();

		$('.sg-location-marker', markerContainer).each(function() {
			var myLatlng = new google.maps.LatLng($(this).attr('data-lat'),$(this).attr('data-lng'));
			bounds.extend(myLatlng);
			var dataId = $(this).attr('data-id');
			var infowindow = new google.maps.InfoWindow({
				content: '<p>' + $(this).attr('data-lat') + ',' + $(this).attr('data-lng') + '</p>'
					+ '<p><a href="' + $(this).attr('data-delete') + '" id="' + dataId + '">' + deleteText + '</a></p>'
			});
			var marker = new google.maps.Marker({
				position: myLatlng,
				map: map,
				icon: 'https://maps.google.com/mapfiles/ms/micons/blue-dot.png'
			});
			google.maps.event.addListener(marker, 'click', function() {
				infowindow.open(map,marker);
				var link = $('#' + dataId);
				link.unbind("click");
				link.click(function(e) {
					// call ajax
					$.get($(this).attr('href'), function (responseText) {
						markerContainer.replaceWith(responseText);
						updateGeocompleteMap(id, $(id + '-markers'), input);
					});
					e.preventDefault();
				});
			});
			segradaMapMarkers[id][segradaMapMarkers[id].length] = marker;
		});

		//contain by map markers
		if (segradaMapMarkers[id].length == 0) {
			map.setZoom(1);
			map.setCenter(new google.maps.LatLng(0, 0));
		} else {
			// zoom in onto the markers
			map.fitBounds(bounds);
		}

		//console.log(segradaMapMarkers);
	}

	// *******************************************************
	// Graph functions

	// Initialize graph
	function graphInitialize() {
		if (!graphInitialized) {
			graphInitialized = true;

			// create a network
			var container = document.getElementById('sg-graph');
			var data = {
				nodes: graphNodes,
				edges: graphEdges
			};
			var options = {
				nodes: {
					color: {
						border: '#000',
						background: '#fff'
					},
					labelHighlightBold: false
				},
				edges: {
					font: {
						size: 6
					},
					labelHighlightBold: false,
					selectionWidth: 0,
					arrows: {
						to: true
					}
				},
				groups: {
					node: {
						shape: 'box'
					}
				}
			};
			graphNetwork = new vis.Network(container, data, options);

			// handle double click
			graphNetwork.on("doubleClick", function(params) {
				var url = null;
				// node double clicked
				if (params.nodes.length > 0) {
					var node = graphNodes.get(params.nodes[0]);
					if (node != null && node.url != null) url = node.url;
				} else if (params.edges.length > 0) {
					var edge = graphEdges.get(params.edges[0]);
					if (edge != null && edge.url != null) url = edge.url;
				}

				// call loader
				if (url != null)
					loadDataAddUrl(url);
			});

			/*if (typeof console != "undefined") {
				console.log("Graph Network was initialized.");
			}*/
		}
	}

	// Hide graph and show control
	function graphHide() {
		var link = $('#sg-toggle-graph');
		if (link.hasClass('active')) {
			link.removeClass('active');
			$('.fa-share-alt-square', link).addClass('fa-share-alt').removeClass('fa-share-alt-square');
			$('#sg-graph-container').hide();
			$('#sg-control').show();

			// brute force: TODO make this work more nicely
			graphNetwork.destroy();
			graphInitialized = false;
		}
	}

	// Show graph and hide control
	function graphShow() {
		var link = $('#sg-toggle-graph');
		if (!link.hasClass('active')) {
			link.addClass('active');
			$('.fa-share-alt', link).addClass('fa-share-alt-square').removeClass('fa-share-alt');
			$('#sg-control').hide();
			$('#sg-graph-container').show();

			// init graph, if needed
			graphInitialize();
		}
	}

	// load remote data, show graph and update it
	function graphLoadRemote(url) {
		// error handling
		if (url == null) {
			alert("Null url!");
			return;
		}

		// show graph
		graphShow();

		// prepare data
		var nodeIds = [];
		var edgeIds = [];

		var temp = graphNodes.get({fields: ['id']});
		for (var i = 0; i < temp.length; i++)
			nodeIds.push(temp[i].id);
		temp = graphEdges.get({fields: ['id']});
		for (var i = 0; i < temp.length; i++)
			edgeIds.push(temp[i].id);

		// post AJAX data
		$.ajax({
			url: url,
			type: "POST",
			dataType: 'json',
			data: JSON.stringify({ "nodes": nodeIds, "edges": edgeIds }),
			success: function(data, textStatus, jqXHR) {
				// error handling TODO: make this nicer!
				if (data == null) {
					alert("NULL data");
					return;
				}
				if (data.error != null) {
					alert(data.error);
					return;
				}
				// no errors: update graph
				if (data.nodes != null && data.nodes.length > 0) graphNodes.update(data.nodes);
				if (data.edges != null && data.edges.length > 0) graphEdges.update(data.edges);

				// TODO: remove edges/nodes

				// select node, if needed
				if (data.highlightNode != null) {
					graphNetwork.unselectAll();
					graphNetwork.selectNodes([ data.highlightNode ], false);
				}
			}
		});
	}

	// *******************************************************
	// add destroyed events when elements are destroyed
	$.event.special.destroyed = {
		remove: function(o) {
			if (o.handler) {
				o.handler()
			}
		}
	};

	// called after document is ready
	$(document).ready(function () {
		// *******************************************************
		// "close all" link
		$('#sg-close-all').click(function (e) {
			$('.sg-data').fadeOut('fast', function () {
				$(this).remove(); // remove after finishing fading out
			});
			e.preventDefault();
		});

		// locale change
		$('.sg-locale').click(function(e) {
			// AJAX call
			$.get($(this).attr('href'), function (data) {
				var url = $('#sg-base').html();
				// reload base url
				if (data != '') window.location.href = url;
			});
			e.preventDefault();
		});

		// *******************************************************
		// initialize tokenizers
		tagsTokenizer.initialize();

		// *******************************************************
		// Do graph toggle
		$('#sg-graph-container').hide();
		$('#sg-toggle-graph').click(function(e) {
			if ($(this).hasClass('active')) graphHide();
			else graphShow();
			e.preventDefault();
		});

		// init defaults
		afterAjax($('body'));
	});
})(jQuery);