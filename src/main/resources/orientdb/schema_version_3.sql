######################################################################################################################
create class UserGroup
create property UserGroup.title String
create property UserGroup.titleasc String
create property UserGroup.roles EMBEDDEDMAP<String, Integer>
create property UserGroup.created Datetime
create property UserGroup.modified Datetime
create property UserGroup.creator Link<User>
create property UserGroup.modifier Link<User>
create property UserGroup.special String
alter property UserGroup.title MANDATORY true
alter property UserGroup.title COLLATE ci
alter property UserGroup.titleasc MANDATORY true
alter property UserGroup.roles MANDATORY true
alter property UserGroup.created MANDATORY true
alter property UserGroup.modified MANDATORY true
alter property UserGroup.special COLLATE ci
create index UserGroup.title UNIQUE_HASH_INDEX
create index UserGroup.titleasc NOTUNIQUE
create index UserGroup.special NOTUNIQUE


######################################################################################################################
create property User.group Link<UserGroup>
alter property User.group MANDATORY true

######################################################################################################################
# to make stuff work:
alter property User.role MANDATORY false
# executed in populate data:
#drop property User.role FORCE