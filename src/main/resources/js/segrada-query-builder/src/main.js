import { createApp } from 'vue'
import App from './App.vue'

// use function because we want to do this dynamically
function createSegradaQueryBuilder (containerId) {
  createApp(App).mount(containerId)
}

// export globally
global.createSegradaQueryBuilder = createSegradaQueryBuilder
