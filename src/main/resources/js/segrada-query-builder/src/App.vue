<template>
  <div class="sg-query-builder-app">
    <button type="button" class="btn btn-default" @click="count++">Add</button>

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
  setup () {
    const { t } = useI18n()
    return { t }
  },
  data () {
    return ({
      count: 1
    })
  }
}
</script>

<style lang="less">
.sg-query-builder-app {
  margin: 0.5em
}
</style>
