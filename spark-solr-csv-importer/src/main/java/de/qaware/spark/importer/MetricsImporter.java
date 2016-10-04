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

import org.apache.spark.api.java.JavaSparkContext;

/**
 * MetricsImporter.
 * Created by weigend on 20.09.16.
 */
public interface MetricsImporter {

    /**
     * Imports Metric CSVs into Solr by using Spark.
     * @param pathUrl the URL could be a local file or path or a hdfs path.
     * @param sparkContext the Spark context.
     *
     */
    void importMetrics(String pathUrl, JavaSparkContext sparkContext);
}
