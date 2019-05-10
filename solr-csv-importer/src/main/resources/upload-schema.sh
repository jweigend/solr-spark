
export SOLR_HOME=/Volumes/BigData2017/software/solr-7.5.0

$SOLR_HOME/bin/solr zk upconfig -n bigdata -d ./conf -z 192.168.1.100:2181
