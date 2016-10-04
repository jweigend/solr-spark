#!/usr/bin/env bash

SOLR=/Volumes/BigData2016/software/solr-6.2.0

# Start Solr on 8983 default port and zookeeper on 9983 default port
$SOLR/bin/solr start -c
# Start 2nd Solr on 8984
$SOLR/bin/solr start -c -z localhost:9983 -p 8984



