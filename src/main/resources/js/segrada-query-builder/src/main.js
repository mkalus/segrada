import { createApp } from 'vue'
import App from './App.vue'
import store from './store'

// use function because we want to do this dynamically
function createSegradaQueryBuilder (containerId) {
  createApp(App).use(store).mount(containerId)
}

// export globally
global.createSegradaQueryBuilder = createSegradaQueryBuilder
