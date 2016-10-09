#!/usr/bin/env bash

# LOCAL
#SOLR=localhost:8983
#curl "http://${SOLR}/solr/admin/collections?action=DELETE&name=ekgdata2"

# REMOTE
SOLR=192.168.1.100:8983
curl "http://${SOLR}/solr/admin/collections?action=DELETE&name=ekgdata3"
