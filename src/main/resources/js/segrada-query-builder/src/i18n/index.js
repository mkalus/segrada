import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    message: {
      buildQueryHeader: 'Create Query',
      selectToAdd: 'Select query to add it to list',
      manualList: 'Manuel List',
      node: 'Node',
      source: 'Source',
      file: 'File',
      nodes: 'Nodes',
      sources: 'Sources',
      files: 'Files',
      searchForTitle: 'Search title or term',
      noResultsFound: 'No results found',
      delete: 'Delete'
    }
  },
  de: {
    message: {
      buildQueryHeader: 'Abfrage erstellen',
      selectToAdd: 'Abfrage wählen, um sie hinzuzufügen',
      manualList: 'Manuelle Liste',
      node: 'Knoten',
      source: 'Quelle',
      file: 'Datei',
      nodes: 'Knoten',
      sources: 'Quellen',
      files: 'Dateien',
      searchForTitle: 'Titel oder Begriff eingeben',
      noResultsFound: 'Keine Datensätze gefunden',
      delete: 'Löschen'
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
