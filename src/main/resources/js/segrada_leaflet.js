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

            console.log(mapSettings.provider);
            console.log(mapSettings.lat);

            //create map
            const map = L.map(id + '-map').setView([mapSettings.lat || 0, mapSettings.lng || 0], mapSettings.zoom || 2);

            // add map from settings
            L.tileLayer.provider(mapSettings.provider || "Stamen.TerrainBackground", mapSettings.options || {}).addTo(map);

            // add form opener
            $('.sg-map-add-marker', container).click(function(e) {
                $(this).parent().hide();
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

            console.log(container);
        });
    } // afterMapAjax end

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