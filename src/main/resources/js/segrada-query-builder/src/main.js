import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import i18n from './i18n'

// use function because we want to do this dynamically
function createSegradaQueryBuilder (containerId) {
  createApp(App).use(store).use(i18n).mount(containerId)
}

// export globally
global.createSegradaQueryBuilder = createSegradaQueryBuilder
