######################################################################################################################
# Source additions: source type and times added to source
create property Source.sourceType String
create property Source.minEntry String
create property Source.maxEntry String
create property Source.minJD Long
create property Source.maxJD Long
create property Source.minEntryCalendar String
create property Source.maxEntryCalendar String

######################################################################################################################
# Source reference additions: role of node in source and color
create property SourceReference.roleOfNode String
create property SourceReference.color Integer
create index SourceReference.roleOfNode NOTUNIQUE

######################################################################################################################
# add indexes for minJD and maxJD
create index Source.minJD NOTUNIQUE
create index Source.maxJD NOTUNIQUE
create index Node.minJD NOTUNIQUE
create index Node.maxJD NOTUNIQUE
create index Relation.minJD NOTUNIQUE
create index Relation.maxJD NOTUNIQUE
