import { createStore } from 'vuex'

export default function initStore (apiEndPoint, data) {
  return createStore({
    state: {
      /**
       * api end point (URL)
       */
      apiEndPoint,
      /**
       * array of objects representing query
       */
      data: (Array.isArray(data) && data) || []
    },
    mutations: {
      addEntry: function (state, type) {
        state.data.push({
          type
        })
      },
      updateEntry: function (state, payload) {
        // copy values
        state.data[payload.idx] = { ...payload.data }
      }
    },
    getters: {
      entryByIndex: (state) => (idx) => {
        return state.data[idx]
      }
    }
  })
}
