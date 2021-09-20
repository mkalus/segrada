<template>
  <div class="sg-query-builder-app">
    <button type="button" class="btn btn-default" @click="increase">Add</button>

    <HelloWorld v-for="n in count" :key="n"/>

    <p>{{ t("message.hello") }}</p>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'App',
  components: {
    HelloWorld
  },
  props: {
    callback: {
      type: Function
    }
  },
  setup () {
    const { t } = useI18n()
    return { t }
  },
  data () {
    return ({
      count: 1
    })
  },
  methods: {
    increase () {
      this.count++
      this.callback(this.count)
    }
  }
}
</script>

<style lang="less">
.sg-query-builder-app {
  margin: 0.5em
}
</style>
