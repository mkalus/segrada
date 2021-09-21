<template>
  <li class="list-group-item">
    <template v-if="entity && field === 'source'">{{ entity.shortTitle }}</template>
    <template v-if="entity && field !== 'source'">{{ entity.title }}</template>

    <button type="button" class="close" aria-label="Close" @click="remove"><span aria-hidden="true">&times;</span></button>
  </li>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import idToUid from '@/lib/id-to-uid'

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
    }
  },
  setup (props) {
    const store = useStore()

    const entity = ref(undefined)

    const getEntity = async () => {
      const response = await fetch(store.state.apiEndPoint + props.field + '/' + idToUid(props.id))
      entity.value = await response.json()
    }

    onMounted(getEntity)

    return { entity, getEntity }
  },
  methods: {
    remove () {
      this.$emit('delete')
    }
  }
}
</script>
