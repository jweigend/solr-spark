#!/usr/bin/env bash

SOLR=/Volumes/BigData2016/software/solr-6.2.0

# LOCAL
ZK_HOST=localhost:9983
$SOLR/server/scripts/cloud-scripts/zkcli.sh -z $ZK_HOST -cmd upconfig -confdir ./ekg_configs/conf -confname ekgdata2

# REMOTE
ZK_HOST=192.168.1.100:2181
$SOLR/server/scripts/cloud-scripts/zkcli.sh -z $ZK_HOST -cmd upconfig -confdir ./ekg_configs/conf -confname ekgdata2


