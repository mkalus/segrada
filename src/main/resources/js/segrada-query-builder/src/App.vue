<template>
  <div class="sg-query-builder-app">
    <h4>{{ t('message.buildQueryHeader') }}</h4>

    <select v-model="currentSelection" style="color: #999" class="form-control" @change="addEntry(currentSelection)">
      <option value="">{{ t('message.selectToAdd') }}</option>
      <option v-for="queryType of possibleQueryTypes" :key="queryType" :value="queryType" style="color: #000">{{ t('message.' + queryType) }}</option>
    </select>

    <template v-for="(dataEntry, idx) in dataEntries" :key="dataEntry.id + idx">
      <ManualList v-if="dataEntry.type === 'manualList'" :idx="idx" @change="callCallback" @delete="deleted(idx)" />
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
  data () {
    return ({
      currentSelection: ''
    })
  },
  computed: {
    dataEntries () {
      return this.$store.state.data
    },
    possibleQueryTypes () {
      return [
        'manualList'
      ]
    }
  },
  methods: {
    callCallback () {
      this.changeCallbackFunction(this.$store.state.data)
    },
    addEntry (type) {
      if (type !== '') {
        this.$store.commit('addEntry', type)
        this.callCallback()
      }

      this.currentSelection = '' // reset
    },
    deleted (idx) {
      this.$store.commit('removeEntry', idx)
      this.callCallback()
    }
  }
}
</script>

<style lang="less">
/**
 * Query builder styles
 */
.sg-query-builder-query-container {
  border: 1px solid #ccc;
  margin-top: 0.5em;

  h5 {
    margin-top: 0;
    background: #ddd;
    padding: 0.5em;
  }
}

.sg-query-builder-query-elements {
  margin: 0.5em;
}

/**
 * Generic styles
 */
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
  padding: 10px;
}

.o-acp__item {
  display: block;
  position: relative;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;

  &:hover {
    background-color: #ddd;
  }
}
</style>
