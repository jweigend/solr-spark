#!/usr/bin/env bash

SOLR=/Volumes/BigData2016/software/solr-6.2.0

# CONFNAME=ekgdata3
CONFNAME=ekgdata3

# LOCAL
# ZK_HOST=localhost:9983
# $SOLR/server/scripts/cloud-scripts/zkcli.sh -z $ZK_HOST -cmd upconfig -confdir ./$CONFNAME/conf -confname $CONFNAME

# REMOTE
ZK_HOST=192.168.1.100:2181
$SOLR/server/scripts/cloud-scripts/zkcli.sh -z $ZK_HOST -cmd upconfig -confdir ./$CONFNAME/conf -confname $CONFNAME


