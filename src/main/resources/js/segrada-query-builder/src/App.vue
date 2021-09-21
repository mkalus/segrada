<template>
  <div class="sg-query-builder-app">
    <button type="button" class="btn btn-default" @click="addEntry('manualList')">Add</button>

    <template v-for="(dataEntry, idx) in dataEntries" :key="idx">
      <ManualList v-if="dataEntry.type === 'manualList'" :idx="idx" @change="callCallback" />
    </template>
  </div>
</template>

<script>
import { useI18n } from 'vue-i18n'
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import ManualList from '@/components/ManualList'

export default {
  name: 'App',
  components: { ManualList },
  props: {
    /**
     * callback function that is called after each change in the query created
     * it receives one parameter: data which is an object representing the query
     */
    changeCallbackFunction: {
      type: Function
    }
  },
  setup (props) {
    const { t } = useI18n()
    const store = useStore()

    // call callback after having mounted the app
    onMounted(() => {
      props.changeCallbackFunction(store.state.data)
    })

    return { t }
  },
  computed: {
    dataEntries () {
      return this.$store.state.data
    }
  },
  methods: {
    callCallback () {
      this.changeCallbackFunction(this.$store.state.data)
    },
    addEntry (type) {
      this.$store.commit('addEntry', type)
      this.callCallback()
    }
  }
}
</script>

<style lang="less">
.sg-query-builder-app {
  margin: 0.5em
}

.mt-2 {
  margin-top: 10px;
}

/**
 * Oruga Autocompleter - see https://oruga.io/components/Autocomplete.html
 */
.o-acp {
  position: relative;
}

.o-ctrl-input {
  position: relative;
}

.o-input__icon-left {
  position: absolute;
  top: 8px;
  height: 100%;
  left: 5px;
}

.o-input-iconspace-left {
  padding-left: 24px;
}

.o-acp__menu {
  display: block;
  position: absolute;
  left: 0;
  top: 100%;
  overflow: auto;
  z-index: 20;
  background-color: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 5px 24px 5px 5px;
}

.o-acp__item {
  display: block;
  position: relative;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}
</style>
