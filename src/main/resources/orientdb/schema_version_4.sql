######################################################################################################################
create class SavedQuery
create property SavedQuery.type String
create property SavedQuery.title String
create property SavedQuery.titleasc String
create property SavedQuery.description String
create property SavedQuery.data String
create property SavedQuery.created Datetime
create property SavedQuery.modified Datetime
create property SavedQuery.user Link User
alter property SavedQuery.title MANDATORY true
alter property SavedQuery.title COLLATE ci
alter property SavedQuery.titleasc MANDATORY true
alter property SavedQuery.created MANDATORY true
alter property SavedQuery.modified MANDATORY true
create index SavedQuery.type NOTUNIQUE
create index SavedQuery.titleasc NOTUNIQUE
create index SavedQuery.user NOTUNIQUE