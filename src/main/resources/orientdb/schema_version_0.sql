######################################################################################################################
create class Config
create property Config.key String
create property Config.value String
alter property Config.key MANDATORY true
alter property Config.value MANDATORY true
create index Config.key DICTIONARY_HASH_INDEX

######################################################################################################################
create class User
#more below

######################################################################################################################
create class Color
create property Color.title String
create property Color.titleasc String
create property Color.color Integer
create property Color.created Datetime
create property Color.modified Datetime
create property Color.creator Link User
create property Color.modifier Link User
alter property Color.title MANDATORY true
alter property Color.title COLLATE ci
alter property Color.titleasc MANDATORY true
alter property Color.color MANDATORY true
alter property Color.created MANDATORY true
alter property Color.modified MANDATORY true
create index Color.color NOTUNIQUE
create index Color.titleasc NOTUNIQUE

######################################################################################################################
create class Pictogram
create property Pictogram.title String
create property Pictogram.titleasc String
create property Pictogram.fileIdentifier String
create property Pictogram.mimeType String
create property Pictogram.created Datetime
create property Pictogram.modified Datetime
create property Pictogram.creator Link User
create property Pictogram.modifier Link User
alter property Pictogram.title MANDATORY true
alter property Pictogram.title COLLATE ci
alter property Pictogram.titleasc MANDATORY true
alter property Pictogram.fileIdentifier MANDATORY true
alter property Pictogram.created MANDATORY true
alter property Pictogram.modified MANDATORY true
create index Pictogram.title on Pictogram (title) FULLTEXT ENGINE LUCENE
create index Pictogram.titleasc NOTUNIQUE

######################################################################################################################
#create class User
create property User.login String
create property User.password String
create property User.name String
create property User.nameasc String
create property User.role String
create property User.lastLogin Datetime
create property User.active Boolean
create property User.created Datetime
create property User.modified Datetime
create property User.creator Link User
create property User.modifier Link User
alter property User.login MANDATORY true
alter property User.password MANDATORY true
alter property User.name MANDATORY true
alter property User.nameasc MANDATORY true
alter property User.role MANDATORY true
alter property User.active MANDATORY true
alter property User.created MANDATORY true
alter property User.modified MANDATORY true
create index User.login UNIQUE_HASH_INDEX
create index User.name NOTUNIQUE
create index User.nameasc NOTUNIQUE

######################################################################################################################
create class Comment extends V
create property Comment.text String
create property Comment.markup String
create property Comment.pictogram Link Pictogram
create property Comment.color Integer
create property Comment.created Datetime
create property Comment.modified Datetime
create property Comment.creator Link User
create property Comment.modifier Link User
alter property Comment.text MANDATORY true
alter property Comment.markup MANDATORY true
alter property Comment.created MANDATORY true
alter property Comment.modified MANDATORY true
#+ Edges: Files, Tags, Comments

######################################################################################################################
create class Tag extends V
create property Tag.title String
create property Tag.titleasc String
create property Tag.created Datetime
create property Tag.modified Datetime
create property Tag.creator Link User
create property Tag.modifier Link User
alter property Tag.title MANDATORY true
alter property Tag.title COLLATE ci
alter property Tag.titleasc MANDATORY true
alter property Tag.created MANDATORY true
alter property Tag.modified MANDATORY true
create index Tag.title UNIQUE_HASH_INDEX
create index Tag.titleasc UNIQUE
create index Tag.searchtitle on Tag (title) FULLTEXT ENGINE LUCENE

######################################################################################################################
create class Location
create property Location.parent Link V
create property Location.latitude Double
create property Location.longitude Double
create property Location.created Datetime
create property Location.modified Datetime
create property Location.creator Link User
create property Location.modifier Link User
alter property Location.parent MANDATORY true
alter property Location.latitude MANDATORY true
alter property Location.longitude MANDATORY true
alter property Location.created MANDATORY true
alter property Location.modified MANDATORY true
create index Location.parent NOTUNIQUE_HASH_INDEX
create index Location.spatial on Location (latitude,longitude) SPATIAL ENGINE LUCENE

######################################################################################################################
create class Period
create property Period.parent Link V
create property Period.type String
create property Period.fromEntry String
create property Period.toEntry String
create property Period.fromJD Long
create property Period.toJD Long
create property Period.fromEntryCalendar String
create property Period.toEntryCalendar String
create property Period.created Datetime
create property Period.modified Datetime
create property Period.creator Link User
create property Period.modifier Link User
alter property Period.parent MANDATORY true
alter property Period.type MANDATORY true
alter property Period.created MANDATORY true
alter property Period.modified MANDATORY true
create index Period.parent NOTUNIQUE_HASH_INDEX
create index Period.fromJD_toJD ON Period (fromJD, toJD) NOTUNIQUE

######################################################################################################################
create class File extends V
create property File.filename String
create property File.title String
create property File.titleasc String
create property File.description String
create property File.descriptionMarkup String
create property File.copyright String
create property File.mimeType String
create property File.location String
create property File.fullText String
create property File.fileSize Long
create property File.indexFullText Boolean
create property File.containFile Boolean
create property File.pictogram Link Pictogram
create property File.color Integer
create property File.created Datetime
create property File.modified Datetime
create property File.creator Link User
create property File.modifier Link User
alter property File.title MANDATORY true
alter property File.title COLLATE ci
alter property File.titleasc MANDATORY true
alter property File.filename MANDATORY true
alter property File.mimeType MANDATORY true
alter property File.descriptionMarkup MANDATORY true
alter property File.created MANDATORY true
alter property File.modified MANDATORY true
alter property File.indexFullText MANDATORY true
alter property File.containFile MANDATORY true
create index File.filename NOTUNIQUE
create index File.searchtitle on File (title,filename) FULLTEXT ENGINE LUCENE
create index File.titleasc NOTUNIQUE
#+ Edges: Files, Tags, Comments

######################################################################################################################
create class Source extends V
create property Source.shortTitle String
create property Source.shortTitleasc String
create property Source.longTitle String
create property Source.shortRef String
create property Source.url String
create property Source.productCode String
create property Source.author String
create property Source.citation String
create property Source.copyright String
create property Source.description String
create property Source.descriptionMarkup String
create property Source.pictogram Link Pictogram
create property Source.color Integer
create property Source.created Datetime
create property Source.modified Datetime
create property Source.creator Link User
create property Source.modifier Link User
alter property Source.shortTitle MANDATORY true
alter property Source.shortTitle COLLATE ci
alter property Source.shortTitleasc MANDATORY true
alter property Source.longTitle COLLATE ci
alter property Source.shortRef MANDATORY true
alter property Source.shortRef COLLATE ci
alter property Source.created MANDATORY true
alter property Source.modified MANDATORY true
create index Source.shortTitle NOTUNIQUE
create index Source.shortTitleAsc NOTUNIQUE
create index Source.shortRef UNIQUE_HASH_INDEX
create index Source.searchtitle on Source (longTitle,shortRef,shortTitle) FULLTEXT ENGINE LUCENE
#+ Edges: Files, Tags, Comments

######################################################################################################################
create class Node extends V
create property Node.title String
create property Node.titleasc String
create property Node.alternativeTitles String
create property Node.description String
create property Node.descriptionMarkup String
create property Node.pictogram Link Pictogram
create property Node.color Integer
create property Node.created Datetime
create property Node.modified Datetime
create property Node.creator Link User
create property Node.modifier Link User
create property Node.minEntry String
create property Node.maxEntry String
create property Node.minJD Long
create property Node.maxJD Long
create property Node.minEntryCalendar String
create property Node.maxEntryCalendar String
alter property Node.title MANDATORY true
alter property Node.title COLLATE ci
alter property Node.titleasc MANDATORY true
alter property Node.alternativeTitles COLLATE ci
alter property Node.description MANDATORY true
alter property Node.descriptionMarkup MANDATORY true
alter property Node.created MANDATORY true
alter property Node.modified MANDATORY true
create index Node.title NOTUNIQUE
create index Node.titleasc NOTUNIQUE
create index Node.alternativeTitles NOTUNIQUE
create index Node.searchtitle ON Node (title,alternativeTitles) FULLTEXT ENGINE LUCENE
#+ Edges: Files, Tags, Comments
#+ Transient links: Locations, Periods, SourceReferences

######################################################################################################################
create class RelationType extends V
create property RelationType.fromTitle String
create property RelationType.toTitle String
create property RelationType.fromTitleAsc String
create property RelationType.toTitleAsc String
create property RelationType.fromTags Linklist Tag
create property RelationType.toTags Linklist Tag
create property RelationType.description String
create property RelationType.descriptionMarkup String
create property RelationType.pictogram Link Pictogram
create property RelationType.color Integer
create property RelationType.created Datetime
create property RelationType.modified Datetime
create property RelationType.creator Link User
create property RelationType.modifier Link User
alter property RelationType.fromTitle MANDATORY true
alter property RelationType.fromTitle COLLATE ci
alter property RelationType.toTitle MANDATORY true
alter property RelationType.toTitle COLLATE ci
alter property RelationType.fromTitleAsc MANDATORY true
alter property RelationType.toTitleAsc MANDATORY true
alter property RelationType.description MANDATORY true
alter property RelationType.descriptionMarkup MANDATORY true
alter property RelationType.created MANDATORY true
alter property RelationType.modified MANDATORY true
create index RelationType.fromTitle NOTUNIQUE
create index RelationType.toTitle NOTUNIQUE
create index RelationType.fromTitleAsc NOTUNIQUE
create index RelationType.toTitleAsc NOTUNIQUE
create index RelationType.fromTitle_toTitle ON RelationType (fromTitle,toTitle) FULLTEXT ENGINE LUCENE
create index RelationType.fromTags NOTUNIQUE_HASH_INDEX
create index RelationType.toTags NOTUNIQUE_HASH_INDEX
#+ Edges: Tags

######################################################################################################################
create class IsRelation extends E

create class Relation extends V
create property Relation.relationType Link RelationType
create property Relation.relationLink Link IsRelation
create property Relation.description String
create property Relation.descriptionMarkup String
create property Relation.pictogram Link Pictogram
create property Relation.color Integer
create property Relation.created Datetime
create property Relation.modified Datetime
create property Relation.creator Link User
create property Relation.modifier Link User
create property Relation.minEntry String
create property Relation.maxEntry String
create property Relation.minJD Long
create property Relation.maxJD Long
create property Relation.minEntryCalendar String
create property Relation.maxEntryCalendar String
alter property Relation.relationType MANDATORY true
alter property Relation.relationLink MANDATORY true
alter property Relation.description MANDATORY true
alter property Relation.descriptionMarkup MANDATORY true
alter property Relation.created MANDATORY true
alter property Relation.modified MANDATORY true
create index Relation.relationType NOTUNIQUE_HASH_INDEX
create index Relation.relationLink UNIQUE_HASH_INDEX
#+ Edges: Files, Tags, Comments
#+ Transient links: Locations, Periods, SourceReferences

######################################################################################################################
create class SourceReference
create property SourceReference.source Link Source
create property SourceReference.reference Link V
create property SourceReference.referenceText String
create property SourceReference.created Datetime
create property SourceReference.modified Datetime
create property SourceReference.creator Link User
create property SourceReference.modifier Link User
alter property SourceReference.source MANDATORY true
alter property SourceReference.reference MANDATORY true
alter property SourceReference.created MANDATORY true
alter property SourceReference.modified MANDATORY true
create index SourceReference.referenceText NOTUNIQUE
create index SourceReference.source NOTUNIQUE_HASH_INDEX
create index SourceReference.reference NOTUNIQUE_HASH_INDEX

######################################################################################################################
create class IsTagOf extends E
create class IsCommentOf extends E
create class IsFileOf extends E

#TODO: inferences