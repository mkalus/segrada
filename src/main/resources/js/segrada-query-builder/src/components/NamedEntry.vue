<template>
  <span v-if="tag" class="label label-info sg-query-builder-label">
    <template v-if="entity && field === 'source'">{{ entity.shortTitle }}</template>
    <template v-if="entity && field !== 'source'">{{ entity.title }}</template>

    <button type="button" class="btn btn-link sg-query-builder-close-btn" @click="remove" :title="t('message.delete')"><span aria-hidden="true">&times;</span></button>
  </span>
  <li v-else class="list-group-item">
    <template v-if="entity && field === 'source'">{{ entity.shortTitle }}</template>
    <template v-if="entity && field !== 'source'">{{ entity.title }}</template>

    <button type="button" class="close" @click="remove" :title="t('message.delete')"><span aria-hidden="true">&times;</span></button>
  </li>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import idToUid from '@/lib/id-to-uid'
import { useI18n } from 'vue-i18n'

export default {
  name: 'NamedEntry',
  props: {
    id: {
      type: String,
      required: true
    },
    field: {
      type: String,
      required: true
    },
    tag: {
      type: Boolean,
      default: false
    }
  },
  setup (props) {
    const store = useStore()
    const { t } = useI18n()

    const entity = ref(undefined)

    const getEntity = async () => {
      const response = await fetch(store.state.apiEndPoint + props.field + '/' + idToUid(props.id))
      entity.value = await response.json()
    }

    onMounted(getEntity)

    return { t, entity, getEntity }
  },
  methods: {
    remove () {
      this.$emit('delete')
    }
  }
}
</script>
