import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    message: {
      node: 'Node',
      source: 'Source',
      file: 'File',
      searchForTitle: 'Search title or term',
      noResultsFound: 'No results found'
    }
  },
  de: {
    message: {
      node: 'Knoten',
      source: 'Quelle',
      file: 'Datei',
      searchForTitle: 'Titel oder Begriff eingeben',
      noResultsFound: 'Keine Datensätze gefunden'
    }
  }
}

export default function (locale) {
  return createI18n({
    legacy: false, // use composition api
    locale: locale, // set locale
    fallbackLocale: 'en',
    messages
  })
}
