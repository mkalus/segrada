function escapeHTML(myString) {
	if (typeof myString == 'string') return myString.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
	return '';
}

(function ($) {
	/**
	 * Substring matcher for remote searches for nodes
	 * @param searchUrl
	 */
	const genericMatcher = function(searchUrl) {
		return function findMatches(q, cb) {
			$.ajax({
				url: searchUrl + encodeURIComponent(q)
			}).done(function (data) {
				cb(data);
			});
		}
	};


	/**
	 * Substring matcher for remote searches for nodes
	 */
	const nodeMatcher = function() {
		return function findMatches(q, cb) {
			let searchUrl = urlSegradaNodeSearch + encodeURIComponent(q);

			// get current textField
			const textField = $(".sg-node-search").filter(":focus");
			const relationTypeSelect = $("#" + textField.attr('data-select-id') + ' option').filter(":selected");
			const contraintIds = relationTypeSelect.attr(textField.attr('data-attr'));
			if (contraintIds != null && contraintIds.length > 0) {
				// add list of ids
				searchUrl += '&tags=' + encodeURIComponent(contraintIds);
			}

			$.ajax({
				url: searchUrl
			}).done(function (data) {
				cb(data);
			});
		}
	};

	/**
	 * Matcher for tags (tag input)
	 */
	const tagMatcher = genericMatcher(urlSegradaTagSearch);

	/**
	 * Matcher function for tags (tag input)
	 * @param q
	 */
	const matchTags = function(q) {
		return new Promise((resolve, reject) => {
			tagMatcher(q, function(data) {
				resolve(data.map(d => d.title));
			});
		});
	}

	/**
	 * creates a typeahead instance on $this
	 * @param $this
	 * @param target
	 * @param source
	 * @returns {*}
	 */
	const createTypeAhead = function($this, target, source) {
		return $this.typeahead({
			source: source,
			items: 'all',
			matcher: () => true,
			displayText: (item) => item.title,
			autoSelect: false,
			delay: 200,
			afterSelect: function(item) {
				target.val(item.id);
			}
		}).change(function() {
			if ($this.val() === '') {
				target.val(null);
			}
		});
	};

	// clean certain urls from jsessionid elements
	function cleanPathFromSessionId(path) {
		return path.replace(/;jsessionid=([^?]*)/, '');
	}

	urlSegradaPictogramSearch = cleanPathFromSessionId(urlSegradaPictogramSearch);
	urlSegradaPictogramFile = cleanPathFromSessionId(urlSegradaPictogramFile);
	urlSegradaTagSearch = cleanPathFromSessionId(urlSegradaTagSearch);
	urlSegradaNodeSearch = cleanPathFromSessionId(urlSegradaNodeSearch);
	urlSegradaFileSearch = cleanPathFromSessionId(urlSegradaFileSearch);
	urlSegradaSourceSearch = cleanPathFromSessionId(urlSegradaSourceSearch);
	urlSegradaRelationAdd = cleanPathFromSessionId(urlSegradaRelationAdd);

	// is graph initialized?
	let graphInitialized = false;
	// global graph data
	const graphNodes = new vis.DataSet([]);
	const graphEdges = new vis.DataSet([]);
	let graphNetwork = null; // reference to graph network
	let graphName = null; // current name of graph
	let graphUid = null; // uid of saved graph

	/**
	 * on enter pressed event
	 * @param func
	 * @returns {jQuery}
	 */
	$.fn.onEnter = function (func) {
		this.bind('keypress', function (e) {
			if ((e.key !== undefined && e.key === 'Enter') || e.keyCode === 13) func.apply(this, [e]);
		});
		return this;
	};

	// will find first id of string (used in ajax)
	const idRegex = new RegExp(/<([^\s]+).*?id="([^"]*?)".*?>/i);

	/**
	 * helper function to create pictogram chooser elements
	 * @param modal containing chooser
	 * @param modalContent content part of modal
	 * @param myId of element
	 * @param part to replace stuff in
	 * @param term to search for
	 */
	function segradaPictogramChooser(modal, modalContent, myId, part, term) {
		const search = urlSegradaPictogramSearch + encodeURIComponent(term);
		$.getJSON(search, function (data) {
			const items = [];
			$.each(data, function (idx, element) {
				const encodedTitle = $('<div/>').text(element.title).html();
				items.push('<div class="col-xs-1 sg-no-padding-right"><a class="sg-pictogram-modal-link" href="#" data-id="' + element.id + '" data-uid="' + element.uid + '" title="' + encodedTitle + '"><img src="' + urlSegradaPictogramFile + element.uid + '" width="24" height="24" alt="' + encodedTitle + '" /></a></div>');
			});
			modalContent.html("<div class='row'>" + items.join("") + "</div>");
			$('a', modalContent).click(function (e2) {
				const picId = $(this).attr('data-id');
				const picUid = $(this).attr('data-uid');
				const picEncodedTitle = $('<div/>').text($(this).attr('title')).html();
				$("#value-" + myId, part).val(picId);
				// add preview
				$("#preview-" + myId, part).html('<img src="' + urlSegradaPictogramFile + picUid + '" width="24" height="24" alt="' + picEncodedTitle + '" /> ' + picEncodedTitle);
				// show hide button
				$("#clear-" + myId, part).show();
				e2.preventDefault();
				modal.modal('hide');
			});
		}).fail(function() {
			alert("ERROR");
		});
	}

	/**
	 * load url and show in data add area of page
	 * @param url
	 */
	function loadDataAddUrl(url) {
		$.get(url, function (data) {
			// find id and hide duplicate elements
			const matches = data.match(idRegex);
			if (matches!=null&&matches.length >= 2) {
				$('#' + matches[2]).remove();
			}

			const container = $('#sg-data');
			container.prepend(data);

			const addedChild = container.children(":first");
			// call after AJAX event
			afterAjax(addedChild);
			// scroll to element
			$('html, body').animate({
				scrollTop: addedChild.offset().top
			}, 500);
		}).fail(function() {
			alert("ERROR");
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
				let target = $form.attr('data-target-id');
				if (typeof target == "undefined" || target === null || target.length === 0) {
					target = '#sg-control';

					// hide graph
					graphHide();
				}
				const container = $(target);
				// disable container
				container.wrapInner("<div class='sg-disabled'></div>")
				container.prepend($('#sg-wait').html());
				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// dynamic target?
				let target = $form.attr('data-target-id');
				if (typeof target == "undefined" || target == null || target.length === 0) target = '#sg-control';
				const container = $(target);
				container.html(responseText);
				afterAjax(container);
			},
			error: function (responseText, statusText, xhr, $form) {
				// dynamic target?
				let target = $form.attr('data-target-id');
				if (typeof target == "undefined" || target == null || target.length === 0) target = '#sg-control';
				const container = $(target);
				container.html(responseText.statusText);
				alert("Error " + responseText.status + "\n" + responseText.statusText);
			}
		});

		// auto submit form element
		$('.sg-submit-form', part).change(function() {
			// find form
			$(this).closest("form").submit();
		});

		// *******************************************************
		// add content to control area
		$('.sg-control-set', part).click(function (e) {
			const $this = $(this);

			let target = $this.attr('data-target-id');
			if (typeof target == "undefined" || target == null || target.length == 0) {
				target = '#sg-control';

				// hide graph
				graphHide();
			}

			// define container and set waiting icon
			const container = $(target);
			container.wrapInner("<div class='sg-disabled'></div>")
			container.prepend($('#sg-wait').html());

			// AJAX call
			$.get($this.attr('href'), function (data) {
				container.html(data); // replace html in container
				// call after AJAX event
				afterAjax(container);
			}).fail(function() {
				alert("ERROR");
			});
			e.preventDefault();
		});

		// *******************************************************
		// double click data handler
		$('[data-data-dblclick]', part).dblclick(function () {
			// AJAX call
			$.get($(this).attr('data-data-dblclick'), function (data) {
				// find id and hide duplicate elements
				const matches = data.match(idRegex);
				if (matches!=null&&matches.length >= 2) {
					$('#' + matches[2]).remove();
				}

				const container = $('#sg-data');
				container.prepend(data);

				const addedChild = container.children(":first");
				// call after AJAX event
				afterAjax(addedChild);
				// scroll to element
				$('html, body').animate({
					scrollTop: addedChild.offset().top
				}, 500);
			}).fail(function() {
				alert("ERROR");
			});
		});

		// *******************************************************
		// delete confirmation
		$('tr [data-confirm]', part).click(function (e) {
			const $this = $(this);

			if (confirm($this.attr('data-confirm'))) {
				// remove tr dynamically
				const row = $this.closest('tr');
				row.addClass("sg-disabled");

				// AJAX call
				$.get($this.attr('href'), function (data) {
					// delete row
					row.slideUp('fast', function() {
						row.remove();
					});
				}).fail(function() {
					alert("ERROR");
				});
			}
			e.preventDefault();
		});

		$('.sg-control-confirm', part).click(function (e) {
			const $this = $(this);

			if (confirm($this.attr('data-confirm'))) {
				const target = $('#' + $this.attr('data-target'));
				target.addClass("sg-disabled");

				// AJAX call
				$.get($this.attr('href'), function (data) {
					target.fadeOut('slow', function () {
						target.remove(); // remove after finishing fading out
					});
				}).fail(function() {
					alert("ERROR");
				});
			}
			e.preventDefault();
		});

		// *******************************************************
		// load tab contents dynamically
		$('.sg-replace-content', part).on('shown.bs.tab', function (e) {
			const $content = $($(this).attr('href'));
			const url = $(this).attr('data-url');

			// load via ajax
			$.get(url, function(content) {
				$content.html(content);
				// call after AJAX event
				afterAjax($content);
			}).fail(function() {
				alert("ERROR");
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
		$(".sg-pictogram-modal", part).on('shown.bs.modal', function () {
			const modal = $(this);
			const myId = modal.attr('id');
			const modalContent = $("#container-" + myId, modal);
			const inputField = $("#filter-" + myId, modal);

			// listener for chooser
			inputField.on('input propertychange paste', function () {
				segradaPictogramChooser(modal, modalContent, myId, part, $(this).val());
			}).onEnter(function () { // pressed enter
				// get first image
				const firstImg = $(".sg-pictogram-modal-link", modalContent).first();
				if (firstImg.length > 0) { // if defined, load first image
					const picId = firstImg.attr('data-id');
					const picUid = firstImg.attr('data-id');
					const picEncodedTitle = $('<div/>').text(firstImg.attr('title')).html();
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
			const myId = $(this).attr('data-id');
			$("#value-" + myId, part).val('');
			$("#preview-" + myId, part).html('');
			$(this).hide();
			e.preventDefault();
		});

		// *******************************************************
		// source ref editor modal
		$(".sg-source-ref-modal", part).on('shown.bs.modal', function () {
			const modal = $(this);
			const modalContent = $(".modal-body", modal);

			$.get(modal.attr('data-href'), function (data) {
				modalContent.html(data);
				// activate color picker
				$("select.sg-colorpicker", modal).simplepicker({
					theme: 'fontawesome'
				});
				$('form', modalContent).ajaxForm({
					beforeSubmit: function(arr, $form, options) {
						// disable container
						$form.wrapInner("<div class='sg-disabled'></div>")
						$form.prepend($('#sg-wait').html());
						return true;
					},
					success: function (responseText, statusText, xhr, $form) {
						// replace target by response text
						const target = $(modal.attr('data-target'));
						target.html(responseText);
						afterAjax(target);
						modal.modal('hide');
						// buggy closing of modal, reason could not be found, so we do it by hand
						$('.modal-backdrop').remove() // delete backdrop of modal
						$('body').removeClass('modal-open'); // delete overflow hidden
					},
					error: function (responseText, statusText, xhr, $form) {
						alert("Error " + responseText.status + "\n" + responseText.statusText);
					}
				});
			}).fail(function() {
				alert("ERROR");
			});
		}).on('hidden.bs.modal', function () {
			$(".modal-body", $(this)).html($('#sg-wait').html()); // replace by waiting icon
		});
		// source ref editor
		$(".sg-source-ref-editor", part).click(function (e) {
			const myModal = $('#' + $(this).attr('data-id'));
			myModal.attr("data-href", $(this).attr('href'));
			myModal.modal('show');
			e.preventDefault();
		});

		// *******************************************************
		// contractable tag list
		$('.sg-taglist-contract', part).each(function() {
			const tags = $('span', $(this));
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
		$("select.sg-tags", part).each(function() {
			const $this = $(this);

			// add input field before myself
			const inputField = document.createElement('input');
			inputField.setAttribute('type', 'text')
			inputField.setAttribute('class', 'form-control')
			$this.after(inputField);

			// hide myself
			$this.hide();

			// initialize tagger
			const myTagger = tagger(inputField, {
				wrap: true,
				completion: {
					list: matchTags,
					min_length: 1,
					delay: 200
				}
			});

			// initialize with values
			const existingValues = $this.val();
			if (existingValues && existingValues.length) {
				existingValues.map(v => myTagger.add_tag(v));
			}

			inputField.addEventListener('add_tag', function(e) {
				$this.append($('<option>', {text: e.item, value: e.item, selected: true}));
			});
			inputField.addEventListener('remove_tag', function(e) {
				$('option', $this).each(function() {
					if ( $(this).val() === e.item) {
						$(this).remove();
					}
				});
			});
		});

		// $("select.sg-tags", part).each(function() {
		// 	const elem = $(this);
		// 	elem.tagsinput({
		// 		confirmKeys: [13], //enter only
		// 		itemValue: function(item) {
		// 			return item.title;
		// 		},
		// 		itemText: function(item) {
		// 			return item.title;
		// 		},
		// 		typeahead: {
		// 			source: matchTags,
		// 			afterSelect: function (item) {
		// 				elem.tagsinput('add', {selected: true, title: item.title}); // workaround
		// 				$(this.$element).val("");
		// 			},
		// 			items: 'all',
		// 			delay: 200,
		// 			autoSelect: false
		// 		}
		// 	});
		// 	elem.on('beforeItemAdd', function(event) {
		// 		// workaround for cursor keys
		// 		if (!event.item || !event.item.selected) {
		// 			event.cancel = true;
		// 		}
		// 	});
		//
		// 	/*elem.on('itemRemoved', function(event) {
		// 		// really delete tag - fix bug in tagsinput
		// 		elem.find('option[value="' + event.item + '"]').remove();
		// 	});*/
		// });

		// *******************************************************
		// node selector for relation forms
		$("input.sg-node-search", part).each(function() {
			const $this = $(this);
			const target = $('#' + $this.attr('data-id'));

			createTypeAhead($this, target, nodeMatcher());
		});

		// *******************************************************
		// node selector for relation forms
		$("input.sg-file-search", part).each(function() {
			const $this = $(this);
			const target = $('#' + $this.attr('data-id'));

			createTypeAhead($this, target, genericMatcher(urlSegradaFileSearch));
		});

		// source selector for forms
		$("input.sg-source-search", part).each(function() {
			const $this = $(this);
			const target = $('#' + $this.attr('data-id'));

			createTypeAhead($this, target, genericMatcher(urlSegradaSourceSearch));
		});

		// bind external links
		$(".sg-link-external", part).click(function(e) {
			const url = $(this).attr('href');
			const win = window.open(url, '_blank');
			win.focus();
			e.preventDefault();
		});

		// *******************************************************
		// bind data forms (left side)
		$("form.sg-data-form", part).ajaxForm({
			beforeSubmit: function (arr, $form, options) {
				// disable form elements
				$(":input", $form).attr("disabled", true);
				// add waiting wheel to button
				$('button.btn-primary', $form).append(' ' + $('#sg-wait-btn').html());

				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// determine target to replace
				let target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.replaceWith(responseText);

				// find id and rerun bindings
				const matches = responseText.match(idRegex);
				if (matches!=null&&matches.length >= 2) {
					afterAjax($('#' + matches[2]));
				}
			},
			error: function (responseText, statusText, xhr, $form) {
				$(":input", $form).attr("disabled", false);
				alert("Error " + responseText.status + "\n" + responseText.statusText);
			}
		});

		// second type of data form (left side)
		$("form.sg-simple-form", part).ajaxForm({
			beforeSubmit: function (arr, $form, options) {
				// disable form elements
				$(":input", $form).attr("disabled", true);

				// determine target to replace
				let target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.html($('#sg-wait'));

				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				// determine target to replace
				let target = $form.attr('data-id');
				if (typeof target !== 'undefined') target = $('#' + target);
				target = target || $form;

				target.html(responseText);

				$(":input", $form).attr("disabled", false);
			},
			error: function (responseText, statusText, xhr, $form) {
				$(":input", $form).attr("disabled", false);
				alert("Error " + responseText.status + "\n" + responseText.statusText);
			}
		});

		// *******************************************************
		// period handler
		$('.sg-periods').each(function() {
			const container = $(this);
			const id = container.attr('id');

			const form = $('.sg-period-form', container);

			// show forms
			$('.sg-period-add', container).click(function(e) {
				$(this).hide();
				const myForm = $('.sg-period-form-add', container);
				myForm.show();

				// show/hide period field
				$('.sg-period-form-period', myForm).change(function(e) {
					if ($(this).is(':checked')) $('.sg-period-toggle', myForm).show();
					else $('.sg-period-toggle', myForm).hide();
				});

				e.preventDefault();
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
					afterAjax($('#' + id));
				},
				error: function (responseText, statusText, xhr, $form) {
					container.removeClass('disabled');
					alert("Error " + responseText.status + "\n" + responseText.statusText);
				}
			});
		});

		// *******************************************************
		// Generic AJAX modal activator
		$('.sg-ajax-modal', part).click(function(e) {
			const $modal = $('#sg-modal');
			const $content = $('.modal-body-inner', $modal);
			const $loading = $('.modal-loading', $modal);
			// set title and body
			$('h4', $modal).html($(this).attr('data-title'));
			// clear content, set loading icon
			$content.html('');
			$content.hide();
			$loading.show();
			$modal.modal('show');
			// dispatch ajax
			$.get($(this).attr('href'), function (data) {
				$loading.hide();
				$content.html(data);
				$content.show();
				afterAjax($content);
			}).fail(function() {
				alert("ERROR");
			});
			e.preventDefault();
		});

		// modal ajax form submit
		$('.sg-ajax-modal-form', part).ajaxForm({
			beforeSubmit: function(arr, $form, options) {
				const $modal = $('#sg-modal');
				const $content = $('.modal-body-inner', $modal);
				const $loading = $('.modal-loading', $modal);
				// hide content, set loading icon
				$content.hide();
				$loading.show();
				return true;
			},
			success: function (responseText, statusText, xhr, $form) {
				let $modal = $('#sg-modal');
				const $content = $('.modal-body-inner', $modal);
				const $loading = $('.modal-loading', $modal);
				$content.html(responseText);
				$content.show();
				$loading.hide();
				afterAjax($content);

				// called after modal shown: update period definitions
				$modal = $('#sg-modal'); // reload html
				$('.sg-update-period', $modal).each(function() {
					const ref = $(this).attr('data-id');
					const target = $(ref);
					if (target.length > 0) {
						for (let i = 0; i < 3; i++) {
							$('td:eq(' + i + ')', target).html($('div:eq(' + i + ')', $(this)).html());
						}
					}
				});
			},
			error: function (responseText, statusText, xhr, $form) {
				const $modal = $('#sg-modal');
				const $content = $('.modal-body-inner', $modal);
				const $loading = $('.modal-loading', $modal);
				$content.html(responseText);
				$content.show();
				$loading.hide();
				afterAjax($content);
			}
		});

		// *******************************************************
		// HTML Editor enabler/disabler
		$('.sg-plain-editor', part).click(function(e) {
			$($(this).attr('data-editor')).summernote('destroy');
		});

		$('.sg-html-editor', part).each(function() {
			let locale = $('html').attr('lang');
			if (locale === 'en') locale = 'en-US';

			const summernoteOptions = {
				lang: locale
			};

			const $this = $(this);
			const editor = $($this.attr('data-editor'));

			// enable if checkbox has been checked
			if ($this.attr('checked')) editor.summernote(summernoteOptions);

			$this.click(function(e) {
				editor.summernote(summernoteOptions);
			});
		});

		// *******************************************************
		// Image Viewer/Lightbox
		$('.sg-lg-image', part).each(function() {
			const id = $(this).attr('id');

			if (id) {
				new Viewer(document.getElementById(id), {
					navbar: false,
					toolbar: {
						zoomIn: 1,
						zoomOut: 1,
						oneToOne: 1,
						reset: 1,
						prev: 0,
						play: {
							show: 1,
							size: 'large'
						},
						next: 0,
						rotateLeft: 1,
						rotateRight: 1,
						flipHorizontal: 1,
						flipVertical: 1
					}
				});
			}
		});

		// *******************************************************
		// Activator for query builder (Vue component)
		$('.sg-query-builder', part).each(function() {
			createSegradaQueryBuilder('#' + $(this).attr('id'));
		});

		// *******************************************************
		// Graph: load remote data and update graph view
		$('a.sg-graph-update', part).click(function(e) {
			// update graph by remotely getting updated data
			graphLoadRemote($(this).attr('href'));
			e.preventDefault();
		});

		$('a.sg-graph-replace', part).click(function(e) {
			// replace graph by remotely getting updated data
			graphClear();
			graphLoadRemote($(this).attr('href'));

			// optionally load title and uid
			const title = $(this).attr('data-title');
			if (title.length !== 0) graphName = title;
			const uid = $(this).attr('data-uid');
			if (uid.length !== 0) graphUid = uid;
			e.preventDefault();
		});

		// Tag hierarchy graph
		$('div.sg-tag-hierarchy', part).each(function() {
			const me = $(this);
			const container = $('.sg-tag-hierarchy-graph', me);
			// hide/show stuff
			$('.sg-child-tags', me).hide();
			container.addClass('sg-margin-top sg-margin-bottom');

			// create new nodes/edges
			const hierarchy = $('.sg-tag-hierarchy-data', me);

			// center node
			const centerId = hierarchy.attr('data-center');
			let countParents = 0;
			let countChildren = 0;

			// cycle through dub divs
			let tagNodes = [];
			let tagEdges = [];

			$('div', hierarchy).each(function() {
				const n = $(this);
				const myId = n.attr('data-id');
				const myLevel = n.attr('data-level');
				tagNodes.push({id: myId, label: n.html(), level: myLevel, url: n.attr('data-url')});
				if (myLevel === '0') {
					tagEdges.push({from: myId, to: centerId });
					countParents++;
				} else if (myLevel === '2') {
					tagEdges.push({from: centerId, to: myId });
					countChildren++;
				}
			});

			// dynamic height of container box
			const countNodes = countParents>countChildren?(countParents===0?1:countParents):countChildren;
			container.css({width: '100%', height: (countNodes*50) + 'px', border: '1px solid #ccc'});

			// create a network
			const data = {
				nodes: new vis.DataSet(tagNodes),
				edges: new vis.DataSet(tagEdges)
			};
			tagNodes = null; tagEdges = null;

			const options = {
				edges: {
					smooth: {
						type:'cubicBezier',
						forceDirection: 'horizontal',
						roundness: 0.6
					}
				},
				nodes: {
					shape: 'box',
				},
				layout: {
					hierarchical:{
						direction: 'LR'
					}
				}
			};

			const tagNetwork = new vis.Network(container.get(0), data, options);

			// handle double click
			tagNetwork.on("doubleClick", function(params) {
				let url = null;
				// node double clicked
				if (params.nodes.length > 0) {
					const tagNode = data.nodes.get(params.nodes[0]);
					if (tagNode != null && tagNode.url != null) url = tagNode.url;
				}

				// call loader
				if (url != null)
					loadDataAddUrl(url);
			});
		});

		// call afterAjaxHooks
		for (let i = 0; i < afterAjaxHooks.length; i++) {
			afterAjaxHooks[i](part); // call function with part parameter
		}
	} // afterAJAX end

	// *******************************************************
	// Graph functions

	let graphPhysics = true;

	// Initialize graph
	function graphInitialize() {
		if (!graphInitialized) {
			graphInitialized = true;

			// create a network
			const container = document.getElementById('sg-graph');
			const data = {
				nodes: graphNodes,
				edges: graphEdges
			};
			const options = {
				locale: $('html').attr('lang'),
				locales: {
					de: {
						edit: 'Änderungsmodus',
						del: 'Lösche Auswahl',
						back: 'Zurück',
						addNode: 'Knoten hinzufügen',
						addEdge: 'Verknüpfung hinzufügen',
						editNode: 'Knoten editieren',
						editEdge: 'Verknüpfung editieren',
						addDescription: 'Klicke auf eine freie Stelle, um einen neuen Knoten zu platzieren.',
						edgeDescription: 'Klicke auf einen Knoten und ziehe die Verknüpfung zu einem anderen Knoten, um diese zu verbinden.',
						editEdgeDescription: 'Klicke auf die Verbindungspunkte und ziehe diese auf einen Knoten, um sie zu verbinden.',
						createEdgeError: 'Es ist nicht möglich, Verknüpfungen mit Clustern zu verbinden.',
						deleteClusterError: 'Cluster können nicht gelöscht werden.',
						editClusterError: 'Cluster können nicht editiert werden.'
					},
					en: {
						edit: 'Toggle edit',
						del: 'Delete selected',
						back: 'Back',
						addNode: 'Add Node',
						addEdge: 'Add Relation',
						editNode: 'Edit Node',
						editEdge: 'Edit Relation',
						addDescription: 'Click in an empty space to place a new node.',
						edgeDescription: 'Click on a node and drag the relation to another node to connect them.',
						editEdgeDescription: 'Click on the control points and drag them to a node to connect to it.',
						createEdgeError: 'Cannot link relations to a cluster.',
						deleteClusterError: 'Clusters cannot be deleted.',
						editClusterError: 'Clusters cannot be edited.'
					}
				},
				manipulation: {
					enabled: true,
					addNode: false,
					addEdge: function(data, callback) {
						// get relation add url
						const url = urlSegradaRelationAdd
							.replace("XFROMX", data.from.replace(/#([0-9]+):([0-9]+)/, "$1-$2"))
							.replace("XTOX", data.to.replace(/#([0-9]+):([0-9]+)/, "$1-$2"))
							.replace("&amp;", "&");
						loadDataAddUrl(url);
						return false;
					},
					editNode: function(data, callback) { return false },
					editEdge: function(data, callback) { return false },
					deleteNode: function(data, callback) { return false },
					deleteEdge: function(data, callback) { return false },
				},
				nodes: {
					shape: 'icon',
					widthConstraint: {
						maximum: 120
					}
				},
				edges: {
					font: {
						size: 10
					},
					labelHighlightBold: false,
					arrows: {
						to: {
							enabled: true
						}
					}
					//smooth: {
					//	type: 'cubicBezier'
					//}
				},
				groups: {
					node: {
						// individual icons via JSON
					},
					tag: {
						shape: 'box',
						color: {
							border: '#5bc0de',
							background: '#5bc0de',
							highlight: {
								background: '#2B7CE9'
							}
						},
						font: {
							color: '#ffffff',
							size: 12
						}
					}
				},
				physics: {
					barnesHut: {
						springLength: 150,
						damping: 0.4,
						avoidOverlap: 0.5
					}
				},
				layout: {
					improvedLayout: true
				}
			};
			graphNetwork = new vis.Network(container, data, options);

			// handle double click
			graphNetwork.on("doubleClick", function(params) {
				let url = null;
				// node double clicked
				if (params.nodes.length > 0) {
					const node = graphNodes.get(params.nodes[0]);
					if (node != null && node.url != null) url = node.url;
				} else if (params.edges.length > 0) {
					const edge = graphEdges.get(params.edges[0]);
					if (edge != null && edge.url != null) url = edge.url;
				}

				// call loader
				if (url != null)
					loadDataAddUrl(url);
			});

			const contextMenu = $('#sg-graph-context-menu');

			// Context menu for graph nodes
			graphNetwork.on("oncontext", function(obj) {
				if (obj.event) {
					obj.event.preventDefault();
				}

				// try to select node
				const nodeId = graphNetwork.getNodeAt(obj.pointer.DOM)
				if (nodeId) {
					graphNetwork.selectNodes([nodeId]);

					const node = graphNodes.get(nodeId);

					// add context menu....
					contextMenu.css({left: obj.event.clientX + 'px', top: obj.event.clientY + 'px'});

					// replace URLs
					const nodeIdForLinks = nodeId.substr(1).replace(':', '-');

					// set behaviour of context links
					const contextMenuLinks = $('a', contextMenu);
					contextMenuLinks.each(function() {
						const $this = $(this);
						if ($this.hasClass('sg-data-add'))
							$this.attr('href', node.url);
						else if ($this.hasClass('sg-graph-update'))
							$this.attr('href', $this.attr('data-href').replaceAll('XXUIDXX', nodeIdForLinks));
						else if ($this.hasClass('sg-graph-node-fix')) {
							if (node.physics !== undefined && !node.physics) $this.hide();
							else {
								$this.show();
								$this.attr('data-id', nodeId);
							}
						}
						else if ($this.hasClass('sg-graph-node-unfix')) {
							if (node.physics === undefined || node.physics) $this.hide();
							else {
								$this.show();
								$this.attr('data-id', nodeId);
							}
						}
					});
					contextMenuLinks.click(function() {
						contextMenu.hide();
					});

					contextMenu.show();
				} else {
					graphNetwork.unselectAll();
				}
			});

			// click outside context menu - hide it!
			$(document).mousedown(function(e) {
				if (contextMenu.is(":visible") && !contextMenu.is(e.target) && contextMenu.has(e.target).length === 0) {
					contextMenu.hide();
				}
			});

			// fix and unfix nodes
			$('.sg-graph-node-fix').click(function(e) {
				const nodeId = $(this).attr('data-id');
				if (nodeId) {
					const node = graphNodes.get(nodeId);
					node.physics = false;

					graphNodes.update(node);
				}
				e.preventDefault();
			});
			$('.sg-graph-node-unfix').click(function(e) {
				const nodeId = $(this).attr('data-id');
				if (nodeId) {
					const node = graphNodes.get(nodeId);
					node.physics = true;

					graphNodes.update(node);
				}
				e.preventDefault();
			});
		}
	}

	// Hide graph and show control
	function graphHide() {
		const link = $('#sg-toggle-graph');
		if (link.hasClass('active')) {
			link.removeClass('active');
			$('.fa-project-diagram', link).removeClass('text-danger');

			// remember positions
			graphNetwork.storePositions();

			$('#sg-graph-container').hide();
			$('#sg-control').show();

			// brute force: TODO make this work more nicely
			graphNetwork.destroy();
			graphInitialized = false;
		}
	}

	// Show graph and hide control
	function graphShow() {
		const link = $('#sg-toggle-graph');
		if (!link.hasClass('active')) {
			link.addClass('active');
			$('.fa-project-diagram', link).addClass('text-danger');
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
		graphShowLoading();

		const csrf = $('#sg-graph-container').attr('data-csrf');

		// post AJAX data
		$.ajax({
			url: url,
			type: "POST",
			dataType: 'json',
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-Token': csrf
			},
			data: JSON.stringify({ "nodes": graphNodes.getIds(), "edges": graphEdges.getIds() }),
			success: function(data, textStatus, jqXHR) {
				graphHideLoading();
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

				//remove edges/nodes
				if (data.removeNodes != null && data.removeNodes.length > 0) graphNodes.remove(data.removeNodes);
				if (data.removeEdges != null && data.removeEdges.length > 0) graphEdges.remove(data.removeEdges);

				// select node, if needed
				if (data.highlightNode != null) {
					graphNetwork.unselectAll();
					graphNetwork.selectNodes([ data.highlightNode ], false);
				}

				graphNetwork.fit();
			}
		});
	}

	// clear graph data
	function graphClear() {
		graphEdges.clear();
		graphNodes.clear();
		graphUid = "";
		graphName = "";
	}

	function graphShowLoading() {
		const $sgGraph = $('#sg-graph');
		$sgGraph.css('background', 'url("' + $sgGraph.attr('data-bg') + '") no-repeat center center');
	}

	function graphHideLoading() {
		$('#sg-graph').css('background', 'transparent');
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
				const url = $('#sg-base').html();
				// reload base url
				if (data !== '') window.location.href = url;
			}).fail(function() {
				alert("ERROR");
			});
			e.preventDefault();
		});

		// *******************************************************
		// Do graph toggle
		$('#sg-toggle-graph').click(function(e) {
			if ($(this).hasClass('active')) graphHide();
			else graphShow();
			e.preventDefault();
		});
		$('.sg-graph-action-remove').click(function(e) {
			const selection = graphNetwork.getSelection();
			if (selection.edges.length > 0) graphEdges.remove(selection.edges);
			if (selection.nodes.length > 0) graphNodes.remove(selection.nodes);
			e.preventDefault();
		});
		$('#sg-graph-action-restart').click(function(e) {
			graphClear();
			e.preventDefault();
		});
		$('#sg-graph-action-reload').click(function(e) {
			graphNetwork.destroy();
			graphInitialized = false;
			graphInitialize();

			// post data to refresh endpoint
			$.post($(this).attr('href'), {
					'_csrf': $('#sg-graph-container').attr('data-csrf'),
					'data': JSON.stringify({
						nodes: graphNodes.getIds(),
						edges: graphEdges.getIds()
					})
				},
				function(data) {
					// check physics of nodes and save positions
					if (data && data.nodes && data.nodes.length) {
						for (let i = 0; i < data.nodes.length; i++) {
							const nodeInGraph = graphNodes.get(data.nodes[i].id);
							if (nodeInGraph) {
								if (!nodeInGraph.physics) data.nodes[i].physics = false;
								data.nodes[i].x = nodeInGraph.x;
								data.nodes[i].y = nodeInGraph.y;
							}
						}
					}

					graphNodes.clear();
					graphEdges.clear();

					graphNodes.update(data.nodes);
					graphEdges.update(data.edges);
				}).fail(function() {
				alert("ERROR");
			});

			e.preventDefault();
		});
		$('#sg-graph-action-fit').click(function(e) {
			graphNetwork.fit();
			e.preventDefault();
		});
		$('#sg-graph-close').click(function(e) {
			graphHide();
			e.preventDefault();
		});
		$('#sg-graph-action-load').click(function(e) {
			$('#sg-graph-modal-load').modal();

			e.preventDefault();
		});
		$('#sg-graph-toggle-physics').click(function(e) {
			graphPhysics = !graphPhysics;

			$(this).html(graphPhysics ? '<i class="fa fa-snowflake"></i>' : '<i class="fa fa-magnet"></i>')

			graphNodes.forEach(function(node) {
				node.physics = graphPhysics;

				graphNodes.update(node);
			});

			e.preventDefault();
		});
		$('#sg-graph-action-save').click(function(e) {
			// any nodes?
			if (graphNodes.length > 0) {
				// prefill form
				$('#title-sg-graph-save').val(graphName !== null ? graphName : '');
				const saveAsNew = $('#save-as-new-sg-graph-save').parent().parent().parent();
				if (graphUid !== null) saveAsNew.show();
				else saveAsNew.hide();

				// show form modal
				$('#sg-graph-modal-save').modal();

			}
			e.preventDefault();
		});

		$("#sg-graph-modal-load").on('shown.bs.modal', function () {
			const $modal = $(this);
			const $content = $('.modal-body', $modal);

			// show loading icon
			$content.html($('#sg-wait').html());

			// load from graph
			const url = $modal.attr('data-url');
			$.get(url, function (data) {
				let getUrl = $modal.attr('data-get-url');
				let content = "";

				// get each saved graph entry
				data.forEach(function(savedGraph) {
					content += "<li><a href='" + getUrl + savedGraph.uid + "' data-uid='" + savedGraph.uid + "'>" + savedGraph.title + "</a></li>"
				});

				// surround by ul
				if (content != "") content = "<ul>" + content + "</ul>";

				$content.html(content);

				// handle clicks
				$('a', $content).click(function(e) {
					// clear graph and load new graph
					graphClear();
					graphLoadRemote($(this).attr('href'));

					// set variables
					graphName = $(this).html();
					graphUid = $(this).attr('data-uid');

					// hide modal
					$modal.modal('hide');

					e.preventDefault();
				});
			});
		});

		// handle save graph form submission - create new or save existing graph
		$('#sg-graph-modal-save-frm').submit(function(e) {
			e.preventDefault();

			const title = $('#title-sg-graph-save').val();
			let uid = graphUid;
			// save as new?
			if (graphUid != null && $('#save-as-new-sg-graph-save').is(':checked')) uid = null;

			// validate
			//TODO: title mandatory -> red feedback on emtpy title

			// remember positions
			graphNetwork.storePositions();

			// store all information in data object
			const nodes = [];
			const edges = [];
			graphNodes.forEach(function(n) {
				nodes.push({
					id: n.id,
					x: n.x,
					y: n.y,
					group: n.group,
					physics: n.physics
				});
			});
			graphEdges.forEach(function(e) {
				edges.push({
					id: e.id,
					group: e.group
				});
			});
			const data = { };

			// save graph
			$.post($(this).attr('action'), {
				'_csrf': $('#sg-graph-container').attr('data-csrf'),
				'uid': uid,
				'title': title,
				'type': 'graph',
				'data': JSON.stringify({
					'nodes': nodes,
					'edges': edges
				})
			}, function(data) {
				// save variables for later
				graphName = title;
				graphUid = data;
				//TODO: User feedback: Save succeeded or the like
			}).fail(function() {
				alert("ERROR");
			});

			// hide modal
			$('#sg-graph-modal-save').modal('hide');
		});

		// init defaults
		afterAjax($('body'));
	});
})(jQuery);