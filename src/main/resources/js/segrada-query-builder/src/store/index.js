import { createStore } from 'vuex'

export default function initStore (apiEndPoint, locale, data, mapSettings) {
  const realMapSettings = {}
  realMapSettings.provider = (mapSettings && mapSettings.provider) || 'Stamen.TerrainBackground'
  realMapSettings.zoom = (mapSettings && mapSettings.zoom) || 1
  realMapSettings.lat = (mapSettings && mapSettings.lat) || 0
  realMapSettings.lng = (mapSettings && mapSettings.lng) || 0
  realMapSettings.options = (mapSettings && mapSettings.options) || {}

  return createStore({
    state: {
      /**
       * api end point (URL)
       */
      apiEndPoint,
      /**
       * array of objects representing query
       */
      data: (Array.isArray(data) && data) || [],
      /**
       * map settings
       */
      mapSettings: realMapSettings,
      /**
       * locale
       */
      locale
    },
    mutations: {
      addEntry: function (state, type) {
        state.data.push({
          id: Math.random().toString(16).substr(2, 14),
          type
        })
      },
      updateEntry: function (state, payload) {
        // copy values
        state.data[payload.idx] = { ...payload.data }
      },
      removeEntry: function (state, idx) {
        state.data.splice(idx, 1)
      }
    },
    getters: {
      entryByIndex: (state) => (idx) => {
        return state.data[idx]
      }
    }
  })
}
