############################################################
# Docker file to create specific Segrada Solr instance
#
# Based on solr instance
############################################################
FROM solr:6-slim

# create new core
RUN cp -ar /opt/solr/server/solr/configsets/data_driven_schema_configs \
    /opt/solr/server/solr/segrada \
    && mkdir /opt/solr/server/solr/segrada/data \
    && echo 'name=segrada' > /opt/solr/server/solr/segrada/core.properties