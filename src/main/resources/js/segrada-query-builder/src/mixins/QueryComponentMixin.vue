<script>
export default {
  name: 'QueryComponentMixin',
  props: {
    idx: {
      type: Number,
      required: true
    }
  },
  methods: {
    change () {
      // commit to store
      this.$store.commit('updateEntry', {
        idx: this.idx,
        data: this.data
      })

      // emit change event
      this.$emit('change', this.data)
    },
    remove () {
      this.$emit('delete')
    }
  }
}
</script>
