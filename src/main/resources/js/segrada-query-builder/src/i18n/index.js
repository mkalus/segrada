import { createI18n } from 'vue-i18n'

export default createI18n({
  legacy: false, // use composition api
  locale: 'en', // set locale
  messages: {
    en: {
      message: {
        hello: 'hello world'
      }
    },
    de: {
      message: {
        hello: 'こんにちは、世界'
      }
    }
  }
})
