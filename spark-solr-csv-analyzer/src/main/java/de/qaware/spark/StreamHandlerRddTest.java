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
package de.qaware.spark;

import com.lucidworks.spark.rdd.SolrJavaRDD;
import com.lucidworks.spark.rdd.SolrRDD;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Some;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Using the StreamHandler to stream results of a single Solr Server (all shards from that server!)
 * Created by weigend on 04.10.16.
 */
public class StreamHandlerRddTest {
    public static void main(String[] args) throws IOException {

        final String solrZkHost = "192.168.1.100:2181";
        //final String sparkMasterUrl = "local[*]";
        final String sparkMasterUrl = "spark://192.168.1.100:7077";

        SparkConf conf = new SparkConf().setMaster(sparkMasterUrl).setAppName("SparkSolrCsvAnayzer");

        try (JavaSparkContext context = new JavaSparkContext(conf)) {
            context.addJar("./build/libs/spark-solr-csv-analyzer-1.0-SNAPSHOT-all.jar");

            String expr =
                    "search(ekgdata2, " +
                            " q=\"metric:*used\"," +
                            " fl=\"date,metric,value,host,process\"," +
                            " sort=\"date asc\"," +
                            " qt=\"/export\")";

            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("expr", expr);

            SolrRDD streamExprRDD = new SolrRDD(
                    solrZkHost,
                    "ekgdata2",
                    context.sc(),
                    new Some<>("/stream"),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    Some.empty(),
                    new Some<>(solrQuery));

            SolrJavaRDD rdd = new SolrJavaRDD(streamExprRDD);

            System.out.println("Result: " + rdd.count() + " Documents streamed.");

            JavaRDD<SolrDocument> docs = rdd.query("");

            List<SolrDocument> d = docs.take(10);
            System.out.println(Arrays.deepToString(d.toArray()));

        } finally {
            //noinspection ThrowFromFinallyBlock,ResultOfMethodCallIgnored
            System.in.read();

        }
    }
}
