/* Segrada Open Layers stuff */
(function ($) {
	/**
	 * tokenizer/finder for elements on nominatim.openstreetmap.org
	 */
	var placenamesTokenizer = new Bloodhound({
		datumTokenizer: function (d) {
			return Bloodhound.tokenizers.whitespace(d.title);
		},
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			wildcard: '%QUERY',
			url: 'http://nominatim.openstreetmap.org/search?format=json&q=%QUERY',
			transform: function(response) {
				var returnValues = [];
				for (var i = 0; i < response.length; i++) {
					returnValues.push({
						title: response[i].display_name,
						lng: response[i].lon,
						lat: response[i].lat
					});
				}

				return returnValues;
			}
		}
	});

	/**
	 * add a single marker to the map
	 * @param $this
	 * @param vectorSource
	 */
	function addMarkerToMap($this, vectorSource) {
		var lat = parseFloat($this.attr('data-lat'));
		var lng = parseFloat($this.attr('data-lng'));
		var comment = $this.attr('data-comment');
		if (typeof comment != 'undefined' && comment != '') comment = '<p>' + escapeHTML(comment) + '</p>';
		else comment = '';
		var myLatlng = new ol.geom.Point(ol.proj.transform([lng, lat], 'EPSG:4326', 'EPSG:3857'));
		//TODO bounds?
		var iconFeature = new ol.Feature({
			geometry: myLatlng,
			data_id: $this.html(),
			lat: lat,
			lng: lng,
			delete_ok: $this.attr('data-delete-ok')==='1',
			delete_url: $this.attr('data-delete'),
			comment: comment
		});
		vectorSource.addFeature(iconFeature);
	}

	/**
	 * Update map markers on map
	 * @param markerContainer
	 * @param ol.source.Vector vectorSource
	 */
	function updateMap(markerContainer, vectorSource) {
		vectorSource.clear(true);
		console.log(vectorSource);

		// add existing markers
		$('.sg-location-marker', markerContainer).each(function() {
			addMarkerToMap($(this), vectorSource)
		});
	}

	/**
	 * called after ajax calls to update a specific part of the document
	 * @param part
	 */
	function afterMapAjax(part) {
		// default
		part = part || $('body');

		// *******************************************************
		// dynamic map loader
		$('.sg-geotab', part).on('shown.bs.tab', function (e) {
			var target = $(e.target);

			// map created already?
			if (target.attr('data-created') == '1') return;

			// set flag
			target.attr('data-created', '1');

			var id = target.attr('data-locations-id');
			var container = $('#' + id);

			var form = $('.sg-map-form', container);
			var input = $('.sg-geocomplete', form);
			var markerContainer = $('#' + id + '-markers', container);
			var deleteText = $('#' + id + '-delete').html();

			// holds markers
			var vectorSource = new ol.source.Vector({
				//create empty vector
			});

			// add existing markers
			updateMap(markerContainer, vectorSource);

			//create the style
			var iconStyle = new ol.style.Style({
				image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
					anchor: [12, 24],
					anchorXUnits: 'pixels',
					anchorYUnits: 'pixels',
					opacity: 0.75,
					src: urlSegradaBasepath + 'img/marker-icon.png'
				}))
			});

			//create map and show it
			var map = new ol.Map({
				layers: [
					new ol.layer.Tile({source: new ol.source.MapQuest({layer: 'sat'})}),
					new ol.layer.Vector({ // vector layer containing markers
						source: vectorSource,
						style: iconStyle
					})
				],
				view: new ol.View({
					center: [0, 0],
					zoom: 1
				}),
				target: id + '-map'
			});

			map.on('singleclick', function(evt) {
				map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
					var popup = new ol.Overlay.Popup();
					map.addOverlay(popup);

					var content = '<p>' + feature.get('lat') + ',' + feature.get('lng') + '</p>';
					content += feature.get('comment');
					if (feature.get('delete_ok'))
						content += '<p><a href="' + feature.get('delete_url') + '" id="x' + feature.get('data_id') + '-delete" data-action="delete">' + deleteText + '</a></p>';
					popup.show(feature.getGeometry().getCoordinates(), content);

					popup.getElement().addEventListener('click', function(e) {
						var action = e.target.getAttribute('data-action');
						if (action == 'delete') {
							// call confirm
							var deleteConfirmText = $('#' + id + '-delete-confirm').html().replace('{0}', feature.get('lat') + ',' + feature.get('lng'));
							if (confirm(deleteConfirmText)) {
								// call ajax
								$.get(feature.get('delete_url'), function (responseText) {
									markerContainer.replaceWith(responseText);
									markerContainer = $('#' + id + '-markers', container);
									// update map
									updateMap(markerContainer, vectorSource);
								});
								popup.hide();
							}
							e.preventDefault();
						}

					}, false);
				});
			});

			map.on('dblclick', function(evt) {
				var dbClickElement = $('.sg-location-dbl-click', container);
				if (dbClickElement.length > 0) {
					var coordinates = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
					var question = dbClickElement.attr('data-confirm').replace('{0}', coordinates[1] + ',' + coordinates[0]);
					if (confirm(question)) {
						// get url from form
						var action = form.attr('action');
						// get security token
						var csrf = $('input[name=_csrf]').val();
						// AJAX post
						$.post(action, {
							_csrf: csrf,
							lng: coordinates[0],
							lat: coordinates[1],
							comment: ''
						}, function (responseText, textStatus, jqXHR) {
							markerContainer.replaceWith(responseText);
							markerContainer = $('#' + id + '-markers', container);
							// update map
							updateMap(markerContainer, vectorSource);
						});
						//
						evt.preventDefault();
					}
				}
			});

			// add form adder
			$('.sg-map-add-marker', container).click(function(e) {
				$(this).parent().hide();
				form.show();
				e.preventDefault();
			});

			// handle completer
			input.typeahead({hint: true,
				highlight: true,
				minLength: 3
			},{
				name: 'address-suggest-' + id,
				displayKey: 'title',
				valueKey: 'id',
				source: placenamesTokenizer.ttAdapter()
			}).bind('typeahead:selected', function(e, datum) {
				input.val(datum.title);
				$('input[name=lat]', form).val(datum.lat);
				$('input[name=lng]', form).val(datum.lng);
				$('input[type=submit]', form).show();
			}).bind('keyup', function() { // empty on textbox empty
				if (!this.value) {
					input.val('');
					$('input[name=lat]', form).val('');
					$('input[name=lng]', form).val('');
					$('input[type=submit]', form).hide();
				}
			});

			// form submitted
			form.ajaxForm({
				beforeSubmit: function(arr, $form, options) {
					// disable container
					container.addClass('disabled');
					return true;
				},
				success: function (responseText, statusText, xhr, $form) {
					container.removeClass('disabled');
					markerContainer.replaceWith(responseText);
					markerContainer = $('#' + id + '-markers', container);
					// update map
					updateMap(markerContainer, vectorSource);
					// clear form
					input.val('');
					$('input[name=lat]', form).val('');
					$('input[name=lng]', form).val('');
					$('input[name=comment]', form).val('');
					$('input[type=submit]', form).hide();
				},
				error: function (responseText, statusText, xhr, $form) {
					container.removeClass('disabled');
					alert("Error " + responseText.status + "\n" + responseText.statusText);
				}
			});
		});
	} // afterMapAjax end

	// called after document is ready
	$(document).ready(function () {
		// add method to hook
		afterAjaxHooks.push(afterMapAjax);
	});
})(jQuery);


/**
 * OpenLayers 3 Popup Overlay.
 * See [the examples](./examples) for usage. Styling can be done via CSS.
 * https://github.com/walkermatt/ol3-popup
 * @constructor
 * @extends {ol.Overlay}
 * @param {Object} opt_options Overlay options, extends olx.OverlayOptions adding:
 *                              **`panMapIfOutOfView`** `Boolean` - Should the
 *                              map be panned so that the popup is entirely
 *                              within view.
 */
ol.Overlay.Popup = function(opt_options) {

	var options = opt_options || {};

	this.panMapIfOutOfView = options.panMapIfOutOfView;
	if (this.panMapIfOutOfView === undefined) {
		this.panMapIfOutOfView = true;
	}

	this.ani = options.ani;
	if (this.ani === undefined) {
		this.ani = ol.animation.pan;
	}

	this.ani_opts = options.ani_opts;
	if (this.ani_opts === undefined) {
		this.ani_opts = {'duration': 250};
	}

	this.container = document.createElement('div');
	this.container.className = 'ol-popup';

	this.closer = document.createElement('a');
	this.closer.className = 'ol-popup-closer';
	this.closer.href = '#';
	this.container.appendChild(this.closer);

	var that = this;
	this.closer.addEventListener('click', function(evt) {
		that.container.style.display = 'none';
		that.closer.blur();
		evt.preventDefault();
	}, false);

	this.content = document.createElement('div');
	this.content.className = 'ol-popup-content';
	this.container.appendChild(this.content);

	// Apply workaround to enable scrolling of content div on touch devices
	ol.Overlay.Popup.enableTouchScroll_(this.content);

	ol.Overlay.call(this, {
		element: this.container,
		stopEvent: true,
		insertFirst: (options.hasOwnProperty('insertFirst')) ? options.insertFirst : true
	});

};

ol.inherits(ol.Overlay.Popup, ol.Overlay);

/**
 * Show the popup.
 * @param {ol.Coordinate} coord Where to anchor the popup.
 * @param {String} html String of HTML to display within the popup.
 */
ol.Overlay.Popup.prototype.show = function(coord, html) {
	this.setPosition(coord);
	this.content.innerHTML = html;
	this.container.style.display = 'block';
	if (this.panMapIfOutOfView) {
		this.panIntoView_(coord);
	}
	this.content.scrollTop = 0;
	return this;
};

/**
 * @private
 */
ol.Overlay.Popup.prototype.panIntoView_ = function(coord) {

	var popSize = {
			width: this.getElement().clientWidth + 20,
			height: this.getElement().clientHeight + 20
		},
		mapSize = this.getMap().getSize();

	var tailHeight = 20,
		tailOffsetLeft = 60,
		tailOffsetRight = popSize.width - tailOffsetLeft,
		popOffset = this.getOffset(),
		popPx = this.getMap().getPixelFromCoordinate(coord);

	var fromLeft = (popPx[0] - tailOffsetLeft),
		fromRight = mapSize[0] - (popPx[0] + tailOffsetRight);

	var fromTop = popPx[1] - popSize.height + popOffset[1],
		fromBottom = mapSize[1] - (popPx[1] + tailHeight) - popOffset[1];

	var center = this.getMap().getView().getCenter(),
		curPx = this.getMap().getPixelFromCoordinate(center),
		newPx = curPx.slice();

	if (fromRight < 0) {
		newPx[0] -= fromRight;
	} else if (fromLeft < 0) {
		newPx[0] += fromLeft;
	}

	if (fromTop < 0) {
		newPx[1] += fromTop;
	} else if (fromBottom < 0) {
		newPx[1] -= fromBottom;
	}

	if (this.ani && this.ani_opts) {
		this.ani_opts.source = center;
		this.getMap().beforeRender(this.ani(this.ani_opts));
	}

	if (newPx[0] !== curPx[0] || newPx[1] !== curPx[1]) {
		this.getMap().getView().setCenter(this.getMap().getCoordinateFromPixel(newPx));
	}

	return this.getMap().getView().getCenter();

};

/**
 * @private
 * @desc Determine if the current browser supports touch events. Adapted from
 * https://gist.github.com/chrismbarr/4107472
 */
ol.Overlay.Popup.isTouchDevice_ = function() {
	try {
		document.createEvent("TouchEvent");
		return true;
	} catch(e) {
		return false;
	}
};

/**
 * @private
 * @desc Apply workaround to enable scrolling of overflowing content within an
 * element. Adapted from https://gist.github.com/chrismbarr/4107472
 */
ol.Overlay.Popup.enableTouchScroll_ = function(elm) {
	if(ol.Overlay.Popup.isTouchDevice_()){
		var scrollStartPos = 0;
		elm.addEventListener("touchstart", function(event) {
			scrollStartPos = this.scrollTop + event.touches[0].pageY;
		}, false);
		elm.addEventListener("touchmove", function(event) {
			this.scrollTop = scrollStartPos - event.touches[0].pageY;
		}, false);
	}
};

/**
 * Hide the popup.
 */
ol.Overlay.Popup.prototype.hide = function() {
	this.container.style.display = 'none';
	return this;
};
