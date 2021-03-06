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
package de.qaware.spark.importer.spark.util;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Helper to create a Spark context.
 * Created by weigend on 20.09.16.
 */
public class CachedSparkContext  {

    /**
     * Configuration properties.
     */
    private static final String APP_NAME = "SPARK_METRICS_IMPORTER";

    /**
     * Singleton.
     */
    private static CachedSparkContext context;

    /**
     * The context to the Spark cluster.
     */
    private JavaSparkContext jsc;

    /**
     * Constructor initializes the JavaSparkContext.
     * @param sparkMasterUrl the Spark Master Url.
     */
    private CachedSparkContext(String sparkMasterUrl) {
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName(APP_NAME);
        sparkConf.set("spark.executor.memory", "12g");
        sparkConf.setMaster(sparkMasterUrl);
        jsc = new JavaSparkContext(sparkConf);
        jsc.addJar("./build/libs/spark-solr-csv-importer-1.0-SNAPSHOT-all.jar"); // add ourself
    }

    /**
     * @return a valid spark context.
     */
    public JavaSparkContext getSparkContext() {
        return jsc;
    }

    /**
     * @return a cached Spark context.
     * @param sparkMasterUrl the Spark Master Url.
     */
    public synchronized static CachedSparkContext instance(String sparkMasterUrl) {
        if (context == null) {
            context = new CachedSparkContext(sparkMasterUrl);
        }
        return context;
    }

}
