/*
 _____________________________________________________________________________

            Project:    BigData 2016
  _____________________________________________________________________________

         Created by:    Johannes Weigend, QAware GmbH
      Creation date:    September 2016
  _____________________________________________________________________________

          License:      Apache License 2.0
  _____________________________________________________________________________
 */
package de.qaware.spark.importer;

import de.qaware.spark.importer.spark.SimpleSparkSolrMetricsImporter;
import de.qaware.spark.importer.spark.util.CachedSparkContext;
import de.qaware.spark.importer.spark.DenormSparkSolrMetricsImporter;

import java.util.Date;

/**
 * Imports a bunch of CSV files into Solr with the help of a Spark cluster.
 * Created by weigend on 23.09.16.
 */
public class ImporterMain {
    public static void main(String[] args) {

        // final String csvImportPath = "./build/resources/main/data/wls1_jmx_lpapp18.csv";
        // final String solrZkHost = "localhost:9983";
        // final String sparkMasterUrl = "local[8]";
        //final String csvImportPath = "hdfs://192.168.1.100:/csv-all";

        final String csvImportPath = "hdfs://192.168.1.100:/2015-KW-10-01";
        final String solrZkHost = "192.168.1.100:2181";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        Date start = new Date();

        // initialize spark context
        CachedSparkContext sparkContext = CachedSparkContext.instance(sparkMasterUrl);

        // Simple
        MetricsImporter importer = new SimpleSparkSolrMetricsImporter(solrZkHost);

        // Denormalized
        // MetricsImporter importer = new DenormSparkSolrMetricsImporter(solrZkHost);
        importer.importMetrics(csvImportPath, sparkContext.getSparkContext());

        Date end = new Date();

        System.out.println("Import: Time taken - " + (end.getTime() - start.getTime()) + " ms.");
    }
}
