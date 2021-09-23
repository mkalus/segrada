<template>
  <div class="sg-query-builder-query-container sg-query-builder-dynamicQuery">
    <h5>
      {{ t('message.dynamicQuery') }} ({{ t('message.' + data.field + 's') }})

      <button type="button" class="close" aria-label="Close" @click="remove" :title="t('message.delete')" style="margin-top: -3px"><span aria-hidden="true">&times;</span></button>
    </h5>

    <div class="sg-query-builder-query-elements">
      <select v-model="data.field" class="form-control" @change="change">
        <option v-for="field in fields" :value="field" :key="field">{{ t('message.' + field) }}</option>
      </select>

      <div class="form-group mt-2">
        <label>{{ t('message.searchTerm') }}</label>
        <input v-model="data.search" class="form-control" :placeholder="t('message.enterSearchTerm')" @input="change">
      </div>

      <div class="row" v-if="data.field !== 'file'">
        <div class="col-md-6 form-group">
          <label>{{ t('message.start') }}</label>
          <input v-model="data.start" class="form-control" @input="change">
        </div>
        <div class="col-md-6 form-group">
          <label>{{ t('message.stop') }}</label>
          <input v-model="data.stop" class="form-control" @input="change">
        </div>
      </div>

      <div class="checkbox" v-if="data.field !== 'file'">
        <label>
          <input v-model="data.hasGeo" type="checkbox"> {{ t('message.addGeo') }}
        </label>
      </div>
      <query-map v-if="data.hasGeo && data.field !== 'file'" :predefined-data="data.geo" @input="changeGeoShape" />

      <div class="form-group mt-2">
        <label>{{ t('message.tags') }}</label>
        <o-autocomplete
          class="form-control"
          :data="autocompleteData"
          :placeholder="t('message.searchTags')"
          :debounce-typing="200"
          @typing="searchTag"
          field="title"
          @select="addTag"
          :clear-on-select="true"
          iconPack="fas"
          :icon="isLoading ? 'hourglass' : 'tag'"
        >
          <template v-slot:empty>
            {{ t('message.noResultsFound') }}
          </template>
        </o-autocomplete>

        <div v-if="data.tags && data.tags.length" class="mt-2">
          <NamedEntry v-for="(id, idx) in data.tags" :key="id" :id="id" field="tag" tag @delete="deletedTag(idx)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import NamedEntry from '@/components/NamedEntry'
import QueryMap from '@/components/QueryMap'
import QueryComponentMixin from '@/mixins/QueryComponentMixin'

export default {
  name: 'DynamicQuery',
  components: { NamedEntry, QueryMap },
  mixins: [QueryComponentMixin],
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
    if (!('field' in data)) data.field = 'node'
    if (!('start' in data)) data.start = ''
    if (!('stop' in data)) data.stop = ''
    if (!('search' in data)) data.search = ''
    if (!('tags' in data)) data.tags = []
    if (!('hasGeo' in data)) data.hasGeo = false
    // geo is skipped by default
    // data.geo = undefined

    return { t, data }
  },
  computed: {
    fields () {
      return ['node', 'source', 'file']
      // TODO: add relations, too?
    }
  },
  data () {
    return ({
      autocompleteData: [],
      isLoading: false
    })
  },
  methods: {
    searchTag (term) {
      this.isLoading = true

      fetch(this.$store.state.apiEndPoint + 'tag/search?s=' + encodeURIComponent(term))
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
    addTag (option) {
      if (option && option.id) {
        if (this.data.tags.indexOf(option.id) < 0) {
          this.data.tags.push(option.id)

          this.change()
        }
      }
    },
    deletedTag (idx) {
      this.data.tags.splice(idx, 1)

      this.change()
    },
    changeGeoShape (shape) {
      this.data.geo = shape

      this.change()
    }
  }
}
</script>
