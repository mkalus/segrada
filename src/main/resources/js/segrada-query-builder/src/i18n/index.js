import { createI18n } from 'vue-i18n'

export default function (locale) {
  return createI18n({
    legacy: false, // use composition api
    locale: locale, // set locale
    fallbackLocale: 'en',
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
}
