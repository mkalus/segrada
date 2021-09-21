import { createApp } from 'vue'
import App from './App.vue'
import initStore from './store'
import createI18N from './i18n'
import { Autocomplete } from '@oruga-ui/oruga-next'
// import '@oruga-ui/oruga-next/dist/oruga.css'

// use function because we want to do this dynamically
function createSegradaQueryBuilder (containerId, locale = 'en', apiEndPoint = 'http://localhost:8080/', predefinedData = undefined, changeCallbackFunction = () => {}) {
  createApp(App, {
    changeCallbackFunction
  })
    .use(initStore(apiEndPoint, predefinedData))
    .use(createI18N(locale))
    .use(Autocomplete)
    .mount(containerId)
}

// export globally
global.createSegradaQueryBuilder = createSegradaQueryBuilder
