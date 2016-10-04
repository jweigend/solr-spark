package de.qaware.spark.importer;

import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by weigend on 20.09.16.
 */
public interface MetricsImporter {

    /**
     * Imports Metric CSVs into some datasink.
     * @param pathUrl the URL could be a local file or path or a hdfs path.
     * @param sparkContext
     *
     */
    void importMetrics(String pathUrl, JavaSparkContext sparkContext);
}
