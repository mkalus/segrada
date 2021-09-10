/**
 * @type {import('@vue/cli-service').ProjectOptions}
 */
module.exports = {
  // see https://cli.vuejs.org/config/ for more information
  outputDir: '../../../webapp/segrada-query-builder/',
  publicPath: '/segrada-query-builder/',
  productionSourceMap: false, // no source map
  filenameHashing: false, // no file name hashing - we need the app as created
  chainWebpack: config => { // no chunk splitting, single file is ok
    config.optimization.delete('splitChunks')
  }
}
