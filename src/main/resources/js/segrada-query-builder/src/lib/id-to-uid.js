export default function idToUid (id) {
  return id.replace(/^#([0-9]+):([0-9]+)$/, '$1-$2')
}
