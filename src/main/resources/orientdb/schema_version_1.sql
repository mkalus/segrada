######################################################################################################################
# Extend period with fuzzy entries and period comment
create property Period.fromFuzzyFlags String
create property Period.toFuzzyFlags String
create property Period.comment String

create property Node.minFuzzyFlags String
create property Node.maxFuzzyFlags String

create property Relation.minFuzzyFlags String
create property Relation.maxFuzzyFlags String

######################################################################################################################
# Location comment
create property Location.comment String

######################################################################################################################
# Forgotten file identifier added, plus thumbnail
create property File.fileIdentifier String
create property File.thumbFileIdentifier String
