######################################################################################################################
create class UserGroup
create property UserGroup.title String
create property UserGroup.titleasc String
create property UserGroup.roles EMBEDDEDMAP<String, Integer>
create property UserGroup.created Datetime
create property UserGroup.modified Datetime
create property UserGroup.creator Link<User>
create property UserGroup.modifier Link<User>
create property UserGroup.active Boolean
alter property UserGroup.title MANDATORY true
alter property UserGroup.title COLLATE ci
alter property UserGroup.titleasc MANDATORY true
alter property UserGroup.roles MANDATORY true
alter property UserGroup.created MANDATORY true
alter property UserGroup.modified MANDATORY true
alter property UserGroup.active MANDATORY true
create index UserGroup.title UNIQUE_HASH_INDEX
create index UserGroup.titleasc NOTUNIQUE


######################################################################################################################
create property User.group Link<UserGroup>

# executed in populate data:
#drop property User.role FORCE