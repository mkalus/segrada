/* Segrada Leaflet stuff */
(function ($) {
    /**
     * Search string matcher for nominatim place name searches
     */
    const nominatimMatcher = function() {
        return function findMatches(q, _, cb) {
            $.ajax({
                url: 'http://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(q)
            }).done(function (data) {
                const returnValues = [];

                for (let i = 0; i < data.length; i++) {
                    returnValues.push({
                        title: data[i].display_name,
                        lng: data[i].lon,
                        lat: data[i].lat
                    });
                }

                cb(returnValues);
            });
        }
    };

    // keeps map settings
    let mapSettings = {};

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
            const target = $(e.target);

            // map created already?
            if (target.attr('data-created') == '1') return;

            // set flag
            target.attr('data-created', '1');

            // get map container
            const id = target.attr('data-locations-id');
            const container = $('#' + id);

            // get other map features
            const form = $('.sg-map-form', container);
            const input = $('.sg-geocomplete', form);
            let markerContainer = $('#' + id + '-markers', container);

            //create map - disable double click zooming
            const map = L.map(id + '-map', { doubleClickZoom: false }).setView([mapSettings.lat || 0, mapSettings.lng || 0], mapSettings.zoom || 1);

            // add map from settings
            const mapLayer = L.tileLayer.provider(mapSettings.provider || "Stamen.TerrainBackground", mapSettings.options || {});
            mapLayer.addTo(map);

            // markers
            const markers = L.featureGroup().addTo(map);

            // add existing markers
            updateMap(markerContainer, markers, map);

            // add form opener
            $('.sg-map-add-marker', container).click(function(e) {
                $('#' + id + '-add-location-btn').hide();
                form.show();
                e.preventDefault();
            });

            // handle completer
            input.typeahead({
                hint: true,
                highlight: true,
                minLength: 2
            },{
                name: 'address-suggest-' + id,
                displayKey: 'title',
                limit: 100,
                valueKey: 'id',
                source: nominatimMatcher(),
                async: true
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
                    updateMap(markerContainer, markers, map);
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

            // map double clicking: add new marker
            map.on('dblclick', function (e) {
                $('#' + id + '-add-location-btn').hide();
                form.show();

                $('input[name=lat]', form).val(e.latlng.lat);
                $('input[name=lng]', form).val(e.latlng.lng);
                $('input[type=submit]', form).show();

                return false;
            })
        });
    } // afterMapAjax end

    // TODO: click on marker to edit it => draggable or form -> via button?

    /**
     * add a single marker to the map
     * @param $this
     * @param markerContainer
     * @param markers L.featureGroup
     * @param map
     */
    function addMarkerToMap($this, markerContainer, markers, map) {
        const lat = parseFloat($this.attr('data-lat'));
        const lng = parseFloat($this.attr('data-lng'));

        const popupContent = document.createElement("DIV");

        // add comment
        const comment = $this.attr('data-comment');
        if (typeof comment !== 'undefined' && comment !== '') {
            const commentContent = document.createElement("P");
            commentContent.innerText = comment;
            popupContent.appendChild(commentContent);
        }

        // add delete button
        if ($this.attr('data-delete-ok')==='1') {
            const deleteContent = document.createElement("P");
            popupContent.appendChild(deleteContent);

            const deleteButton = document.createElement("BUTTON");
            deleteButton.setAttribute("class", "btn btn-danger btn-xs");
            deleteButton.innerText = $('#sg-delete-text').html();
            deleteContent.appendChild(deleteButton);

            // add delete listener
            deleteButton.addEventListener('click', function (e) {
                e.preventDefault();

                // call confirm deletion
                const deleteConfirmText = $('#sg-really-delete-text').html().replace('{0}', lat + ',' + lng);
                if (confirm(deleteConfirmText)) {
                    // call ajax
                    $.get($this.attr('data-delete'), function (responseText) {
                        const id = markerContainer.attr('id');
                        markerContainer.replaceWith(responseText);
                        markerContainer = $('#' + id);
                        // update map
                        updateMap(markerContainer, markers, map, false);
                    });
                }
            });
        }

        const marker = L.marker([lat, lng]).addTo(markers);
        marker.bindPopup(popupContent);
    }

    /**
     * Update map markers on map
     * @param markerContainer
     * @param markers L.featureGroup
     * @param map
     * @param setBounds set to false to leave map like this after update
     */
    function updateMap(markerContainer, markers, map, setBounds = true) {
        markers.clearLayers();

        // add existing markers
        $('.sg-location-marker', markerContainer).each(function() {
            addMarkerToMap($(this), markerContainer, markers, map)
        });

        // update bounds
        if (setBounds) {
            const bb = markers.getBounds();

            if (bb.isValid()) {
                map.fitBounds(bb.pad(0.05));

                // do not zoom too much!
                if (map.getZoom() > 13) {
                    map.setZoom(13);
                }
            }
        }
    }

    // called after document is ready
    $(document).ready(function () {
        // settings from HTML
        const mapSettingsStr = $('#sg-map-settings').html();
        if (mapSettingsStr) {
            mapSettings = JSON.parse(mapSettingsStr);
            if (mapSettings) {
                // add method to hook
                afterAjaxHooks.push(afterMapAjax);
            }
        }
    });
})(jQuery);