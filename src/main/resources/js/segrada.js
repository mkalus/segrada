(function ($) {
	/**
	 * tags tokenizer
	 */
	var tagsTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: urlSegradaTagSearch + '%QUERY'
	});

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
	 * called after ajax calls to update a specific part of the document
	 * @param part
	 */
	function afterAjax(part) {
		// default
		part = part || $('body');

		// *******************************************************
		// make data elements dynamic
		$(".sg-data").addClass("sg-dynamic-data");

		// make closeable element closable
		var closeable = $(".sg-closeable");
		closeable.prepend('<div class="sg-headbox-right"><a href="#" type="button" class="close sg-data-close" aria-label="Close"><span aria-hidden="true">&times;</span></a></div>');
		closeable.removeClass("sg-closeable");

		// dynamic hide
		$(".sg-dynamic-hide").hide();

		// *******************************************************
		// data add links - add data at top of data container
		$('.sg-data-add', part).click(function (e) {
			// AJAX call
			$.get($(this).attr('href'), function (data) {
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
			});
			e.preventDefault();
		});

		// *******************************************************
		// add content to control area
		$('.sg-control-set', part).click(function (e) {
			// AJAX call
			$.get($(this).attr('href'), function (data) {
				var container = $('#sg-control');
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
			});
		});

		// *******************************************************
		// delete confirmation
		$('[data-confirm]', part).click(function (e) {
			if (confirm($(this).attr('data-confirm'))) {
				if ($(this).hasClass('sg-control-confirm')) {
					// AJAX call
					$.get($(this).attr('href'), function (data) {
						var container = $('#sg-control');
						container.html(data); // replace html in container
						// call after AJAX event
						afterAjax(container);
					});
				}
			}
			e.preventDefault();
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
		// color picker init
		$("select.sg-colorpicker").simplepicker({
			theme: 'glyphicons'
		});

		// *******************************************************
		// Tags fields
		$("select.sg-tags").tagsinput({
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
	}

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

		// *******************************************************
		// bind control form
		$('.sg-control-form').ajaxForm({
			success: function (responseText, statusText, xhr, $form) {
				var container = $('#sg-control');
				container.html(responseText);
				afterAjax(container);
			}
		});

		// *******************************************************
		// initialize tokenizers
		tagsTokenizer.initialize();


		// init defaults
		afterAjax($('body'));
	});
})(jQuery);