/**
 * Convert id to uid (e.g. '#12:13' to '12-13')
 * @param id input value to be converted
 */
export default (id) => id.replace(/^#([0-9]+):([0-9]+)$/, '$1-$2')
