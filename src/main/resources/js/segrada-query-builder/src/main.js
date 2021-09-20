import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import createI18N from './i18n'

// use function because we want to do this dynamically
function createSegradaQueryBuilder (containerId, locale = 'en', changeCallbackFunction = () => {}) {
  createApp(App, {
    changeCallbackFunction
  }).use(store).use(createI18N(locale)).mount(containerId)
}

// export globally
global.createSegradaQueryBuilder = createSegradaQueryBuilder
