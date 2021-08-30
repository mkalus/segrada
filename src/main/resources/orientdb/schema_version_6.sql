######################################################################################################################
# Tags now have synonyms
create property Tag.synonyms String
alter property Tag.synonyms COLLATE ci
create index Tag.synonyms DICTIONARY_HASH_INDEX

DROP INDEX Tag.searchtitle
create index Tag.title_synonyms on Tag (title, synonyms) FULLTEXT ENGINE LUCENE
