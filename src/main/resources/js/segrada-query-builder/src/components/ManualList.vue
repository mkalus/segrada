<template>
  <div>
    <select v-model="data.field" class="form-control" @change="changeField">
      <option v-for="field in fields" :value="field" :key="field">{{ t('message.' + field) }}</option>
    </select>

    <o-autocomplete
      class="form-control mt-2"
      :data="autocompleteData"
      :placeholder="t('message.searchForTitle')"
      :debounce-typing="200"
      @typing="search"
      field="title"
      @select="addEntry"
      :clear-on-select="true"
      iconPack="fas"
      :icon="isLoading ? 'hourglass' : 'search'"
    >
      <template v-slot:empty>
        {{ t('message.noResultsFound') }}
      </template>
    </o-autocomplete>

    <ul v-if="data.ids && data.ids.length" class="list-group mt-2">
      <NamedEntry v-for="(id, idx) in data.ids" :key="id" :id="id" :field="data.field" @delete="deleted(idx)" />
    </ul>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import NamedEntry from '@/components/NamedEntry'

export default {
  name: 'ManualList',
  components: { NamedEntry },
  props: {
    idx: {
      type: Number,
      required: true
    }
  },
  setup (props) {
    const { t } = useI18n()
    const store = useStore()

    // clone data
    const data = reactive({ ...store.getters.entryByIndex(props.idx) })

    // set defaults
    if (!('ids' in data)) data.ids = []
    if (!('field' in data)) data.field = 'node'

    return { t, data }
  },
  data () {
    return ({
      autocompleteData: [],
      isLoading: false
    })
  },
  computed: {
    fields () {
      return ['node', 'source', 'file']
    }
  },
  methods: {
    changeField () {
      // clear ids on change
      this.data.ids = []
      this.change()
    },
    change () {
      // commit to store
      this.$store.commit('updateEntry', {
        idx: this.idx,
        data: this.data
      })

      // emit change event
      this.$emit('change', this.data)
    },
    search (term) {
      this.isLoading = true

      fetch(this.$store.state.apiEndPoint + this.data.field + '/search?s=' + encodeURIComponent(term))
        .then(response => response.json())
        .then(data => {
          this.autocompleteData = data
        })
        .catch(error => {
          throw error
        })
        .finally(() => {
          this.isLoading = false
        })
    },
    addEntry (option) {
      if (option && option.id) {
        if (this.data.ids.indexOf(option.id) < 0) {
          this.data.ids.push(option.id)

          this.change()
        }
      }
    },
    deleted (idx) {
      this.data.ids.splice(idx, 1)

      this.change()
    }
  }
}
</script>
