######################################################################################################################
create class RememberMeToken
create property RememberMeToken.user Link<User>
create property RememberMeToken.selector String
create property RememberMeToken.token String
create property RememberMeToken.expires Datetime
alter property RememberMeToken.user MANDATORY true
alter property RememberMeToken.selector MANDATORY true
alter property RememberMeToken.token MANDATORY true
alter property RememberMeToken.expires MANDATORY true
create index RememberMeToken.selector DICTIONARY_HASH_INDEX
