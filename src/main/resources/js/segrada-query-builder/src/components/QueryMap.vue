<template>
  <div ref="theMap" class="sg-query-builder-map" />
</template>

<script>
/*
 * We will add Leaflet by hand because much of the stuff is not yet comaptible with vue3 (e.g. geoman).
 * So on with good old Leaflet integration by hand.
 */
import { useI18n } from 'vue-i18n'
import { useStore } from 'vuex'
import { onMounted, ref } from 'vue'
import geomanShapeToData from '@/lib/geoman-shape-to-data'

const allGeomanControls = {
  position: 'topleft',
  drawMarker: false,
  drawCircleMarker: false,
  drawPolyline: false,
  drawPolygon: false, // TODO delete this line once polygons are supported by index
  cutPolygon: false
}
const closeGeomanControls = {
  drawRectangle: false,
  // drawPolygon: false, // TODO re-enable once polygons are supported by index
  drawCircle: false
}
const openGeomanControls = {
  drawRectangle: true,
  // drawPolygon: true, // TODO re-enable once polygons are supported by index
  drawCircle: true
}

export default {
  name: 'QueryMap',
  emits: ['input'],
  props: {
    predefinedData: {
      type: Object
    }
  },
  setup (props) {
    const { t } = useI18n()
    const store = useStore()

    const mapSettings = store.state.mapSettings
    const theMap = ref(null) // ref to map above - set in the onMounted step, see below
    const shape = ref(undefined) // points to active shape

    onMounted(() => {
      // initialize map
      const map = L.map(theMap.value, { doubleClickZoom: false }).setView([mapSettings.lat || 0, mapSettings.lng || 0], mapSettings.zoom || 1) // eslint-disable-line

      // add map from settings
      const mapLayer = L.tileLayer.provider(mapSettings.provider || 'Stamen.TerrainBackground', mapSettings.options || {}) // eslint-disable-line
      mapLayer.addTo(map)

      // add Leaflet-Geoman controls with some options to the map
      map.pm.setLang(store.state.locale)
      map.pm.addControls(allGeomanControls)

      // populate with predefined data, if set
      if (props.predefinedData && props.predefinedData.shape) {
        let layer
        switch (props.predefinedData.shape) {
          case 'Circle':
            layer = L.circle([props.predefinedData.lat, props.predefinedData.lng], props.predefinedData.radius, { pmIgnore: false }) // eslint-disable-line
            break
          case 'Rectangle':
            layer = L.rectangle(props.predefinedData.coordinates) // eslint-disable-line
            break
          case 'Polygon':
            layer = L.polygon(props.predefinedData.coordinates) // eslint-disable-line
            break
        }

        if (layer) {
          layer.on('pm:edit', (e) => {
            shape.value = geomanShapeToData(e)
          })
          layer.addTo(map)
          map.fitBounds(layer.getBounds())

          // remove some controls
          map.pm.addControls(closeGeomanControls)
        }
      }

      // listen on events
      map.on('pm:create', (e) => { // ok
        shape.value = geomanShapeToData(e)

        const layer = e.layer
        layer.on('pm:edit', (e) => {
          shape.value = geomanShapeToData(e)
        })

        // remove some controls
        map.pm.addControls(closeGeomanControls)
      })

      map.on('pm:remove', (e) => { // ok
        shape.value = {}

        // re-enable all controls
        map.pm.addControls(openGeomanControls)
      })

      map.on('pm:rotateend', (e) => { // ok
        shape.value = geomanShapeToData(e)
      })
    })

    return { L, t, mapSettings, theMap, shape } // eslint-disable-line
  },
  watch: {
    /**
     * emit changes on shape changes
     * @param newValue
     */
    shape (newValue) {
      this.$emit('input', newValue)
    }
  }
}
</script>

<style lang="less">
.sg-query-builder-map {
  height: 400px;
}
</style>
