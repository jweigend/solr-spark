# solr-spark
Samples for using Solr and Spark together for BigData Analytics. 
Here is the complete showcase of the talk "Leveraging the Power of Solr with Spark" at Lucene Revolution 2016, Boston USA.

## Introduction
The combination of Solr Cloud and Spark is a little cumbersome. There are some details at the classpath level and in the details 
of the Lucidworks spark-solr API. 

This code is tested against the following versions:

- Apache Solr version  6.2.1
- Apache Spark version 1.6.2
- Lucidworks Spark/Solr Library 2.2.1-SNAPSHOT (10/04/2016)

This samples show a parallel import into Solr Cloud with linear scalability and a parallel /export handler based Spark RDD to 
access the search results of Solr in the currently fastest possible and scaleable way.

Johannes Weigend
