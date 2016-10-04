#!/usr/bin/env bash

# LOCAL
# SOLR=localhost:8983
# curl "http://${SOLR}/solr/admin/collections?action=CREATE&name=ekgdata2&numShards=8&replicationFactor=1&maxShardsPerNode=4&collection.configName=ekgdata2"

# REMOTE
SOLR=192.168.1.100:8983
curl "http://${SOLR}/solr/admin/collections?action=CREATE&name=ekgdata2&numShards=20&replicationFactor=1&maxShardsPerNode=4&collection.configName=ekgdata2"

