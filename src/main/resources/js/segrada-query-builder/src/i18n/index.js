import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    message: {
      buildQueryHeader: 'Create Query',
      selectToAdd: 'Select query to add it to list',
      manualList: 'Manual List',
      dynamicQuery: 'Dynamic Query',
      node: 'Node',
      source: 'Source',
      file: 'File',
      nodes: 'Nodes',
      sources: 'Sources',
      files: 'Files',
      searchForTitle: 'Search title or term',
      noResultsFound: 'No results found',
      delete: 'Delete',
      searchTerm: 'Search term',
      enterSearchTerm: 'Enter search term',
      tags: 'Tags',
      searchTags: 'Enter tag name',
      start: 'Start',
      stop: 'Stop',
      addGeo: 'Add geographic query'
    }
  },
  de: {
    message: {
      buildQueryHeader: 'Abfrage erstellen',
      selectToAdd: 'Abfrage wählen, um sie hinzuzufügen',
      manualList: 'Manuelle Liste',
      dynamicQuery: 'Dynamische Abfrage',
      node: 'Knoten',
      source: 'Quelle',
      file: 'Datei',
      nodes: 'Knoten',
      sources: 'Quellen',
      files: 'Dateien',
      searchForTitle: 'Titel oder Begriff eingeben',
      noResultsFound: 'Keine Datensätze gefunden',
      delete: 'Löschen',
      searchTerm: 'Suchbegriff',
      enterSearchTerm: 'Suchbegriff eingeben',
      tags: 'Tags',
      searchTags: 'Tag eingeben',
      start: 'Anfang',
      stop: 'Ende',
      addGeo: 'Geoabfrage hinzuzufügen'
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
