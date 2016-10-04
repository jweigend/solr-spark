package de.qaware.spark.importer;

import de.qaware.spark.importer.spark.CachedSparkContext;
import de.qaware.spark.importer.spark.SparkSolrMetricsImporter;

import java.util.Date;

/**
 * Created by weigend on 23.09.16.
 */
public class ImporterMain {
    public static void main(String[] args) {

        // final String csvImportPath = "./build/resources/main/data/wls1_jmx_lpapp18.csv";
        // final String solrZkHost = "localhost:9983";
        // final String sparkMasterUrl = "local[8]";

        final String csvImportPath = "hdfs://192.168.1.100:/2015-KW-10-01";
        final String solrZkHost = "192.168.1.100:2181";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        Date start = new Date();

        // initialize spark context
        CachedSparkContext sparkContext = CachedSparkContext.instance(sparkMasterUrl);

        MetricsImporter importer = new SparkSolrMetricsImporter(solrZkHost);
        importer.importMetrics(csvImportPath, sparkContext.getSparkContext());

        Date end = new Date();

        System.out.println("Import: Time taken - " + (end.getTime() - start.getTime()) + " ms.");
    }
}
