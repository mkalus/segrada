<template>
  <div class="sg-query-builder-app">
    <button type="button" class="btn btn-default" @click="count++">Add</button>

    <HelloWorld v-for="n in count" :key="n"/>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'

export default {
  name: 'App',
  components: {
    HelloWorld
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
