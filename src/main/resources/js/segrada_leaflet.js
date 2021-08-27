/* Segrada Leaflet stuff */
(function ($) {
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

            const id = target.attr('data-locations-id');
            const container = $('#' + id);

            console.log(mapSettings.provider);
            console.log(mapSettings.lat);

            //create map
            const map = L.map(id + '-map').setView([mapSettings.lat || 0, mapSettings.lng || 0], mapSettings.zoom || 2);

            // add map from settings
            L.tileLayer.provider(mapSettings.provider || "Stamen.TerrainBackground", mapSettings.options || {}).addTo(map);

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